package cl.duoc.mastermartini.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.model.BusinessSize
import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.model.Reservation
import cl.duoc.mastermartini.data.model.SentQuote
import cl.duoc.mastermartini.data.model.User
import cl.duoc.mastermartini.data.repository.AddressRepository
import cl.duoc.mastermartini.data.repository.ContentRepository
import cl.duoc.mastermartini.data.repository.FavoriteRepository
import cl.duoc.mastermartini.data.repository.ProductRepository
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.model.SavedAddress
import cl.duoc.mastermartini.data.repository.QuoteRepository
import cl.duoc.mastermartini.data.repository.ReservationRepository
import cl.duoc.mastermartini.data.repository.UserRepository
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ===================== Mi cuenta =====================

data class AccountUiState(
    val user: User? = null,
    val quotes: List<SentQuote> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val largeText: Boolean = false,
    val highContrast: Boolean = false,
    val salesPhone: String = "",
    val addresses: List<SavedAddress> = emptyList(),
    val favorites: List<Product> = emptyList(),
    /** Pedido que se está volviendo a cargar en el carrito. */
    val repeatingOrderId: Long? = null,
)

class AccountViewModel(
    private val users: UserRepository,
    private val content: ContentRepository,
    private val quotes: QuoteRepository,
    private val reservations: ReservationRepository,
    private val settings: SettingsDataStore,
    private val addresses: AddressRepository,
    private val favorites: FavoriteRepository,
    private val products: ProductRepository,
) : ViewModel() {

    private val repeating = MutableStateFlow<Long?>(null)

    /** Direcciones, productos favoritos y el pedido que se repite, en un solo flujo. */
    private val extras = combine(addresses.addresses, favorites.ids, products.products, repeating) { a, favIds, catalog, rep ->
        Triple(a, catalog.filter { it.id in favIds }, rep)
    }

    val state: StateFlow<AccountUiState> = combine(
        users.currentUser, quotes.sentQuotes, reservations.reservations, settings.settings, extras,
    ) { user, q, r, prefs, (addr, favs, rep) ->
        AccountUiState(
            addresses = addr,
            favorites = favs,
            repeatingOrderId = rep,
            user = user,
            quotes = q,
            reservations = r,
            largeText = prefs.largeText,
            highContrast = prefs.highContrast,
            salesPhone = content.salesPhone(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountUiState())

    init {
        // Pedidos e inscripciones hechos también desde la página web.
        viewModelScope.launch { quotes.refreshOrders() }
        viewModelScope.launch { reservations.refreshEnrollments() }
        viewModelScope.launch { users.refreshProfile() }
        viewModelScope.launch { addresses.refresh() }
        viewModelScope.launch { favorites.refresh() }
        if (products.products.value.isEmpty()) viewModelScope.launch { products.refresh() }
    }

    private val _openCart = Channel<Unit>(Channel.BUFFERED)
    /** Se emite cuando "Repetir pedido" cargó productos y hay que abrir el carrito. */
    val openCart = _openCart.receiveAsFlow()

    /** Vuelve a cargar en el carrito los productos de un pedido anterior. */
    fun repeatOrder(orderId: Long) {
        if (repeating.value != null) return
        repeating.value = orderId
        viewModelScope.launch {
            if (products.products.value.isEmpty()) products.refresh()
            val added = quotes.repeatOrder(orderId, products.products.value)
            repeating.value = null
            when {
                added < 0 -> _messages.send("No se pudo cargar el pedido. Revisa tu conexión.")
                added == 0 -> _messages.send("Los productos de ese pedido ya no tienen stock.")
                else -> {
                    _messages.send("Agregamos $added producto${if (added == 1) "" else "s"} a tu carrito")
                    _openCart.send(Unit)
                }
            }
        }
    }

    fun removeFavorite(productId: String) {
        viewModelScope.launch { favorites.toggle(productId)?.let { _messages.send(it) } }
    }

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun setLargeText(v: Boolean) { viewModelScope.launch { settings.setLargeText(v) } }
    fun setHighContrast(v: Boolean) { viewModelScope.launch { settings.setHighContrast(v) } }

    fun cancelReservation(id: Long) {
        viewModelScope.launch {
            reservations.cancel(id)
            _messages.send("Reserva cancelada")
        }
    }

    fun logout() { viewModelScope.launch { users.logout() } }
}

// ===================== Editar perfil =====================

data class EditProfileUiState(
    val loaded: Boolean = false,
    val name: FieldState = FieldState(),
    val phone: FieldState = FieldState(),
    val businessType: BusinessType? = null,
    val businessSize: BusinessSize? = null,
    val interests: Set<String> = emptySet(),
    val interestsError: String? = null,
    val saving: Boolean = false,
    val failedAttempts: Int = 0,
    val saved: Boolean = false,
)

class EditProfileViewModel(private val users: UserRepository) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val u = users.currentUser.filterNotNull().first()
            _state.value = EditProfileUiState(
                loaded = true,
                name = FieldState(u.name, touched = true),
                phone = FieldState(u.phone, touched = true),
                businessType = u.businessType,
                businessSize = u.businessSize,
                interests = u.interests.toSet(),
            )
        }
    }

    fun onType(t: BusinessType) = _state.update { it.copy(businessType = t) }
    fun onSize(s: BusinessSize) = _state.update { it.copy(businessSize = s) }
    fun onInterest(i: String) = _state.update {
        val set = if (i in it.interests) it.interests - i else it.interests + i
        it.copy(interests = set, interestsError = Validators.atLeast(set, 1, "Elige al menos un tema").error)
    }

    fun save() {
        val s = _state.value
        val v = s.copy(
            interestsError = Validators.atLeast(s.interests, 1, "Elige al menos un tema").error,
        )
        if (v.interestsError != null) {
            _state.value = v.copy(failedAttempts = s.failedAttempts + 1)
            return
        }
        _state.value = v.copy(saving = true)
        viewModelScope.launch {
            users.updateProfile(v.businessType!!, v.businessSize!!, v.interests.toList())
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}

// ===================== Mis direcciones =====================

data class AddressesUiState(
    val addresses: List<SavedAddress> = emptyList(),
    val showForm: Boolean = false,
    val label: FieldState = FieldState(),
    val contactName: FieldState = FieldState(),
    val phone: FieldState = FieldState(),
    val region: FieldState = FieldState(),
    val comuna: FieldState = FieldState(),
    val address: FieldState = FieldState(),
    val saving: Boolean = false,
    val failedAttempts: Int = 0,
    val formError: String? = null,
    val deleting: SavedAddress? = null,
) {
    val canAdd: Boolean get() = addresses.size < AddressRepository.MAX
}

class AddressesViewModel(
    private val users: UserRepository,
    private val repo: AddressRepository,
    val regions: cl.duoc.mastermartini.data.local.RegionCatalog,
) : ViewModel() {
    private val form = MutableStateFlow(AddressesUiState())

    val state: StateFlow<AddressesUiState> = combine(form, repo.addresses) { f, list -> f.copy(addresses = list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddressesUiState())

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init { viewModelScope.launch { repo.refresh() } }

    private val labelRule: (String) -> cl.duoc.mastermartini.domain.validation.ValidationResult = { v ->
        val r = Validators.required(v, "Ponle un nombre, por ejemplo Local o Bodega", min = 2)
        if (!r.isValid) r else Validators.optionalText(v, 40)
    }

    fun openForm() {
        viewModelScope.launch {
            val u = users.currentUser.first()
            form.update {
                AddressesUiState(
                    showForm = true,
                    label = FieldState(if (it.addresses.isEmpty()) "Local" else ""),
                    contactName = FieldState(u?.name.orEmpty()).let { f -> f.update(f.value, Validators::name) },
                    phone = FieldState(u?.phone.orEmpty()).let { f -> f.update(f.value, Validators::phone) },
                )
            }
        }
    }

    fun closeForm() = form.update { it.copy(showForm = false, formError = null) }
    fun onLabel(v: String) = form.update { it.copy(label = it.label.update(v, labelRule)) }
    fun onContact(v: String) = form.update { it.copy(contactName = it.contactName.update(v, Validators::name)) }
    fun onPhone(v: String) = form.update { it.copy(phone = it.phone.update(v, Validators::phone)) }
    fun onRegion(v: String) = form.update {
        val comuna = if (regions.comunas(v).contains(it.comuna.value)) it.comuna else FieldState()
        it.copy(region = it.region.update(v, ::validateRegion).copy(touched = true), comuna = comuna)
    }
    fun onComuna(v: String) = form.update { it.copy(comuna = it.comuna.update(v) { c -> validateComuna(it.region.value, c) }.copy(touched = true)) }
    fun onAddress(v: String) = form.update { it.copy(address = it.address.update(v, Validators::address)) }

    fun save() {
        val s = form.value
        if (s.saving) return
        val v = s.copy(
            label = s.label.touch(labelRule),
            contactName = s.contactName.touch(Validators::name),
            phone = s.phone.touch(Validators::phone),
            region = s.region.touch(::validateRegion),
            comuna = s.comuna.touch { validateComuna(s.region.value, it) },
            address = s.address.touch(Validators::address),
            formError = null,
        )
        if (listOf(v.label, v.contactName, v.phone, v.region, v.comuna, v.address).any { it.error != null }) {
            form.value = v.copy(failedAttempts = s.failedAttempts + 1)
            return
        }
        form.value = v.copy(saving = true)
        viewModelScope.launch {
            val user = users.currentUser.first()
            if (user == null) {
                form.update { it.copy(saving = false, formError = "Inicia sesión para guardar direcciones.") }
                return@launch
            }
            try {
                repo.add(user, cl.duoc.mastermartini.data.repository.AddressDraft(
                    v.label.value, v.contactName.value, v.phone.value, v.region.value, v.comuna.value, v.address.value,
                ))
                form.update { AddressesUiState() }
                _messages.send("Dirección guardada")
            } catch (e: cl.duoc.mastermartini.data.remote.ApiException) {
                form.update { it.copy(saving = false, formError = e.message, failedAttempts = it.failedAttempts + 1) }
            }
        }
    }

    fun askDelete(a: SavedAddress) = form.update { it.copy(deleting = a) }
    fun dismissDelete() = form.update { it.copy(deleting = null) }
    fun confirmDelete() {
        val a = form.value.deleting ?: return
        form.update { it.copy(deleting = null) }
        viewModelScope.launch {
            val msg = try { repo.delete(a.id); "Dirección eliminada" } catch (e: cl.duoc.mastermartini.data.remote.ApiException) { e.message }
            _messages.send(msg)
        }
    }

    private fun validateRegion(v: String) = Validators.oneOf(v, regions.names, "Elige una región")
    private fun validateComuna(region: String, v: String) =
        Validators.oneOf(v, regions.comunas(region), if (region.isBlank()) "Primero elige la región" else "Elige una comuna")
}
