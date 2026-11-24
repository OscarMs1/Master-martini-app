package cl.duoc.mastermartini.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.local.RegionCatalog
import cl.duoc.mastermartini.data.model.QuoteLine
import cl.duoc.mastermartini.data.model.User
import cl.duoc.mastermartini.data.repository.Delivery
import cl.duoc.mastermartini.data.repository.OrderEstimate
import cl.duoc.mastermartini.data.repository.OrderResult
import cl.duoc.mastermartini.data.repository.ProductRepository
import cl.duoc.mastermartini.data.repository.QuoteRepository
import cl.duoc.mastermartini.data.repository.QuoteRequest
import cl.duoc.mastermartini.data.repository.UserRepository
import cl.duoc.mastermartini.device.NotificationHelper
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/** Línea de la cotización con su precio actual del catálogo. */
data class PricedLine(val line: QuoteLine, val unitPrice: Long?) {
    val subtotal: Long get() = (unitPrice ?: 0) * line.quantity
}

/** Resultado del envío para el diálogo final. */
data class SentOrder(val code: String, val approved: Boolean, val total: Long, val summary: String, val notified: Boolean)

data class QuoteFormState(
    val contactName: FieldState = FieldState(),
    val phone: FieldState = FieldState(),
    val region: FieldState = FieldState(),
    val comuna: FieldState = FieldState(),
    val address: FieldState = FieldState(),
    val delivery: Delivery = Delivery.DOMICILIO,
    val dateMillis: Long? = null,
    val dateError: String? = null,
    val notes: FieldState = FieldState(),
    /** Pago simulado (proyecto académico): permite mostrar los dos flujos del backend. */
    val paymentApproved: Boolean = true,
    val loading: Boolean = false,
    val failedAttempts: Int = 0,
    val formError: String? = null,
    val sent: SentOrder? = null,
) {
    val dateLabel: String get() = dateMillis?.let { formatDate(it) }.orEmpty()
}

fun formatDate(millisUtc: Long): String =
    Instant.ofEpochMilli(millisUtc).atZone(ZoneOffset.UTC).toLocalDate()
        .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "CL")))
        .replaceFirstChar { it.uppercase() }

class QuoteFormViewModel(
    private val users: UserRepository,
    private val quotes: QuoteRepository,
    catalog: ProductRepository,
    val regions: RegionCatalog,
    private val notifications: NotificationHelper,
) : ViewModel() {

    val lines: StateFlow<List<PricedLine>> = combine(quotes.lines, catalog.products) { lines, products ->
        lines.map { line -> PricedLine(line, products.firstOrNull { it.id == line.productId }?.finalPrice) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(QuoteFormState())
    val state: StateFlow<QuoteFormState> = _state.asStateFlow()

    private var user: User? = null
    /** Se reutiliza si el envío falla por red, para que el backend no duplique el pedido. */
    private var requestKey = UUID.randomUUID().toString()

    init {
        // Prellenamos con los datos de la cuenta (los mismos que pide la web al pagar).
        viewModelScope.launch {
            val u = users.currentUser.filterNotNull().first()
            user = u
            _state.update {
                it.copy(
                    contactName = FieldState(u.name).let { f -> f.update(f.value, Validators::name) },
                    phone = FieldState(u.phone).let { f -> f.update(f.value, Validators::phone) },
                    region = FieldState(u.region).let { f -> f.update(f.value, ::validateRegion) },
                    comuna = FieldState(u.comuna),
                    address = FieldState(u.address).let { f -> f.update(f.value, Validators::address) },
                ).let { s -> s.copy(comuna = s.comuna.update(s.comuna.value) { c -> validateComuna(s.region.value, c) }) }
            }
        }
        if (catalog.products.value.isEmpty()) viewModelScope.launch { catalog.refresh() }
    }

    fun estimate(lines: List<PricedLine>, delivery: Delivery): OrderEstimate =
        OrderEstimate.of(lines.sumOf { it.subtotal }, delivery)

    fun onContact(v: String) = _state.update { it.copy(contactName = it.contactName.update(v, Validators::name)) }
    fun onPhone(v: String) = _state.update { it.copy(phone = it.phone.update(v, Validators::phone)) }
    fun onRegion(v: String) = _state.update {
        val region = it.region.update(v, ::validateRegion).copy(touched = true)
        // Al cambiar de región, la comuna anterior deja de ser válida.
        val comuna = if (regions.comunas(v).any { c -> c == it.comuna.value }) it.comuna else FieldState()
        it.copy(region = region, comuna = comuna)
    }
    fun onComuna(v: String) = _state.update { it.copy(comuna = it.comuna.update(v) { c -> validateComuna(it.region.value, c) }.copy(touched = true)) }
    fun onAddress(v: String) = _state.update { it.copy(address = it.address.update(v, Validators::address)) }
    fun onDelivery(d: Delivery) = _state.update { it.copy(delivery = d) }
    fun onPayment(approved: Boolean) = _state.update { it.copy(paymentApproved = approved) }
    fun onNotes(v: String) = _state.update { it.copy(notes = it.notes.update(v) { n -> Validators.optionalText(n, 300) }.copy(touched = true)) }
    fun onDate(millis: Long?) = _state.update { it.copy(dateMillis = millis, dateError = Validators.futureWorkingDate(millis).error) }

    fun onContactBlur() = _state.update { it.copy(contactName = it.contactName.touch(Validators::name)) }
    fun onPhoneBlur() = _state.update { it.copy(phone = it.phone.touch(Validators::phone)) }
    fun onAddressBlur() = _state.update { it.copy(address = it.address.touch(Validators::address)) }

    fun removeLine(productId: String) {
        viewModelScope.launch { quotes.removeProduct(productId) }
    }

    fun submit() {
        val s = _state.value
        if (s.loading) return
        val v = s.copy(
            contactName = s.contactName.touch(Validators::name),
            phone = s.phone.touch(Validators::phone),
            region = s.region.touch(::validateRegion),
            comuna = s.comuna.touch { validateComuna(s.region.value, it) },
            address = s.address.touch(Validators::address),
            notes = s.notes.touch { Validators.optionalText(it, 300) },
            dateError = Validators.futureWorkingDate(s.dateMillis).error,
            formError = null,
        )
        val u = user
        val invalid = listOf(v.contactName, v.phone, v.region, v.comuna, v.address, v.notes).any { it.error != null } ||
            v.dateError != null || lines.value.isEmpty() || u == null
        if (invalid) {
            _state.value = v.copy(failedAttempts = s.failedAttempts + 1)
            return
        }
        _state.value = v.copy(loading = true)
        viewModelScope.launch {
            val summary = lines.value.joinToString("\n") { "• ${it.line.productName} ${it.line.format} x${it.line.quantity}" }
            val result = quotes.send(
                QuoteRequest(
                    user = u!!, contactName = v.contactName.value, phone = v.phone.value,
                    region = v.region.value, comuna = v.comuna.value, address = v.address.value,
                    delivery = v.delivery, deliveryDate = v.dateLabel, notes = v.notes.value,
                    paymentApproved = v.paymentApproved, requestKey = requestKey,
                )
            )
            when (result) {
                is OrderResult.Sent -> {
                    requestKey = UUID.randomUUID().toString()
                    val notified = result.approved && notifications.show(
                        "Pedido ${result.code} confirmado",
                        "Pago simulado aprobado. Lo verás en Mi cuenta → Mis pedidos.",
                    )
                    _state.update {
                        it.copy(loading = false, sent = SentOrder(result.code, result.approved, result.total, summary, notified))
                    }
                }
                is OrderResult.Failed -> _state.update {
                    it.copy(loading = false, formError = result.message, failedAttempts = it.failedAttempts + 1)
                }
            }
        }
    }

    fun dismissSent() = _state.update { it.copy(sent = null) }

    private fun validateRegion(v: String) = Validators.oneOf(v, regions.names, "Elige una región")
    private fun validateComuna(region: String, v: String) =
        Validators.oneOf(v, regions.comunas(region), if (region.isBlank()) "Primero elige la región" else "Elige una comuna")
}
