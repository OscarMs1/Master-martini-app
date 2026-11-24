package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.local.QuoteDao
import cl.duoc.mastermartini.data.local.QuoteEntity
import cl.duoc.mastermartini.data.local.QuoteItemEntity
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.model.QuoteLine
import cl.duoc.mastermartini.data.model.SentQuote
import cl.duoc.mastermartini.data.model.User
import cl.duoc.mastermartini.data.remote.ApiClient
import cl.duoc.mastermartini.data.remote.ApiException
import cl.duoc.mastermartini.data.remote.PedidoBody
import cl.duoc.mastermartini.data.remote.PedidoDto
import cl.duoc.mastermartini.data.remote.PedidoItemBody
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

enum class Delivery(val api: String, val label: String) {
    DOMICILIO("domicilio", "Despacho a domicilio"),
    RETIRO("retiro", "Retiro en tienda"),
}

data class QuoteRequest(
    val user: User,
    val contactName: String,
    val phone: String,
    val region: String,
    val comuna: String,
    val address: String,
    val delivery: Delivery,
    val deliveryDate: String,
    val notes: String,
    /** Pago simulado del proyecto académico: no hay cobro real. */
    val paymentApproved: Boolean,
    /** Misma clave en cada reintento: el backend no crea el pedido dos veces. */
    val requestKey: String = UUID.randomUUID().toString(),
)

sealed interface OrderResult {
    data class Sent(val code: String, val approved: Boolean, val total: Long) : OrderResult
    data class Failed(val message: String) : OrderResult
}

/** Totales estimados en la app. El backend recalcula todo con sus precios al crear el pedido. */
data class OrderEstimate(val subtotal: Long, val shipping: Long) {
    val total: Long get() = subtotal + shipping

    companion object {
        const val SHIPPING = 3_990L
        const val FREE_SHIPPING_FROM = 50_000L

        fun of(subtotal: Long, delivery: Delivery) = OrderEstimate(
            subtotal,
            if (delivery == Delivery.RETIRO || subtotal >= FREE_SHIPPING_FROM) 0 else SHIPPING,
        )
    }
}

/**
 * Cotización en curso (carrito, en Room) y pedidos enviados al backend.
 * Al confirmar la cotización se crea un pedido real (POST /api/v1/pedidos) con pago simulado:
 * el backend descuenta stock solo si el pago queda APROBADO.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuoteRepository(
    private val dao: QuoteDao,
    private val settings: SettingsDataStore,
    private val client: ApiClient,
) {
    private val userId: Flow<Long?> = settings.settings.map { it.loggedUserId }

    val lines: Flow<List<QuoteLine>> = userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else dao.observeItems(id).map { list -> list.map { QuoteLine(it.productId, it.productName, it.format, it.quantity) } }
    }

    val sentQuotes: Flow<List<SentQuote>> = userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else dao.observeQuotes(id).map { list ->
            list.map {
                SentQuote(
                    id = it.id,
                    code = code(it.id),
                    itemsSummary = it.itemsSummary,
                    itemCount = it.itemCount,
                    address = it.address,
                    comuna = it.comuna,
                    deliveryDate = it.deliveryDate,
                    createdAt = it.createdAt,
                    status = it.status,
                    total = it.total,
                )
            }
        }
    }

    private suspend fun requireUser(): Long? = settings.settings.first().loggedUserId

    /** Agrega o reemplaza el producto: un producto queda con un solo formato en la cotización. */
    suspend fun setLine(productId: String, productName: String, format: String, quantity: Int) {
        val id = requireUser() ?: return
        require(quantity in 1..999)
        dao.replaceProduct(QuoteItemEntity(id, productId, productName, format, quantity))
    }

    suspend fun removeProduct(productId: String) {
        val id = requireUser() ?: return
        dao.removeProduct(id, productId)
    }

    /** Crea el pedido en el backend. Si el pago se aprueba se vacía el carrito; si no, se conserva. */
    suspend fun send(request: QuoteRequest): OrderResult {
        val id = requireUser() ?: return OrderResult.Failed("Inicia sesión para enviar la cotización.")
        val items = dao.getItems(id)
        if (items.isEmpty()) return OrderResult.Failed("Tu cotización está vacía.")
        val remoteItems = items.mapNotNull { item -> item.productId.toLongOrNull()?.let { PedidoItemBody(it, item.quantity) } }
        if (remoteItems.size != items.size) return OrderResult.Failed("Quita los productos que ya no están en el catálogo.")
        val pedido = try {
            client.llamar {
                crearPedido(
                    PedidoBody(
                        requestKey = request.requestKey,
                        nombre = request.contactName.trim(),
                        email = request.user.email,
                        run = request.user.run,
                        telefono = request.phone.trim(),
                        region = request.region,
                        comuna = request.comuna,
                        direccion = request.address.trim(),
                        entrega = request.delivery.api,
                        pago = if (request.paymentApproved) "APROBADO" else "RECHAZADO",
                        items = remoteItems,
                    )
                )
            }
        } catch (e: ApiException) {
            return OrderResult.Failed(e.message)
        }
        val entity = pedido.toEntity(id, request.deliveryDate, request.notes.trim())
        val approved = pedido.estado == "APROBADO"
        if (approved) dao.saveAndClear(entity) else dao.insertQuote(entity)
        return OrderResult.Sent(code(pedido.id), approved, pedido.total)
    }

    /** Trae los pedidos de la cuenta (también los hechos desde la página web). */
    suspend fun refreshOrders() {
        val id = requireUser() ?: return
        val remote = runCatching { client.llamar { misPedidos() } }.getOrNull() ?: return
        val local = dao.getQuotes(id).associateBy { it.id }
        dao.insertQuotes(remote.map { p ->
            val previous = local[p.id]
            p.toEntity(id, previous?.deliveryDate.orEmpty(), previous?.notes.orEmpty())
        })
    }

    private fun PedidoDto.toEntity(userId: Long, deliveryDate: String, notes: String): QuoteEntity {
        val lineas = lineas.orEmpty()
        return QuoteEntity(
            id = id,
            userId = userId,
            itemsSummary = lineas.joinToString(" · ") { "${it.nombre} x${it.cantidad}" },
            itemCount = lineas.size,
            contactName = "",
            address = if (entrega == Delivery.RETIRO.api) Delivery.RETIRO.label else direccion.orEmpty(),
            comuna = comuna.orEmpty(),
            deliveryDate = deliveryDate,
            notes = notes,
            status = estado,
            total = total,
            createdAt = runCatching { Instant.parse(fecha).toEpochMilli() }.getOrDefault(System.currentTimeMillis()),
        )
    }

    private fun code(id: Long) = "PED-%04d".format(id)
}
