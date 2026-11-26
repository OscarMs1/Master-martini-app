package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.model.SavedAddress
import cl.duoc.mastermartini.data.model.User
import cl.duoc.mastermartini.data.remote.ApiClient
import cl.duoc.mastermartini.data.remote.ApiException
import cl.duoc.mastermartini.data.remote.DireccionBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Datos de una dirección nueva (el correo y el RUN se toman de la cuenta). */
data class AddressDraft(
    val label: String,
    val contactName: String,
    val phone: String,
    val region: String,
    val comuna: String,
    val address: String,
)

/**
 * Direcciones guardadas de la cuenta (GET/POST/DELETE /api/v1/direcciones). Son las mismas que
 * ve la página web al pagar. El backend permite hasta [MAX] por cuenta.
 */
class AddressRepository(private val client: ApiClient) {
    private val _addresses = MutableStateFlow<List<SavedAddress>>(emptyList())
    val addresses: StateFlow<List<SavedAddress>> = _addresses.asStateFlow()

    suspend fun refresh() {
        val list = runCatching { client.llamar { direcciones() } }.getOrNull() ?: return
        _addresses.value = list.map {
            SavedAddress(
                id = it.id,
                label = it.etiqueta.orEmpty(),
                contactName = it.nombre.orEmpty(),
                phone = it.telefono.orEmpty(),
                region = it.region.orEmpty(),
                comuna = it.comuna.orEmpty(),
                address = it.direccion.orEmpty(),
            )
        }
    }

    /** Guarda la dirección. Lanza [ApiException] con el mensaje del backend si falla (p. ej. límite de 5). */
    suspend fun add(user: User, draft: AddressDraft) {
        client.llamar {
            guardarDireccion(
                DireccionBody(
                    etiqueta = draft.label.trim(),
                    nombre = draft.contactName.trim(),
                    email = user.email,
                    run = user.run,
                    telefono = draft.phone.trim(),
                    region = draft.region,
                    comuna = draft.comuna,
                    direccion = draft.address.trim(),
                )
            )
        }
        refresh()
    }

    suspend fun delete(id: Long) {
        client.llamar { borrarDireccion(id) }
        _addresses.update { list -> list.filterNot { it.id == id } }
    }

    fun clear() { _addresses.value = emptyList() }

    companion object { const val MAX = 5 }
}

/**
 * Productos favoritos de la cuenta (/api/v1/favoritos), compartidos con la web.
 * El cambio se muestra al instante y se revierte si el backend lo rechaza.
 */
class FavoriteRepository(private val client: ApiClient) {
    private val _ids = MutableStateFlow<Set<String>>(emptySet())
    val ids: StateFlow<Set<String>> = _ids.asStateFlow()

    suspend fun refresh() {
        val list = runCatching { client.llamar { favoritos() } }.getOrNull() ?: return
        _ids.value = list.map { it.productoId.toString() }.toSet()
    }

    /** Devuelve null si salió bien, o el mensaje de error. */
    suspend fun toggle(productId: String): String? {
        val remoteId = productId.toLongOrNull() ?: return "Este producto no admite favoritos."
        val wasFavorite = productId in _ids.value
        _ids.update { if (wasFavorite) it - productId else it + productId }
        return try {
            if (wasFavorite) client.llamar { quitarFavorito(remoteId) } else client.llamar { agregarFavorito(remoteId) }
            null
        } catch (e: ApiException) {
            _ids.update { if (wasFavorite) it + productId else it - productId }
            e.message
        }
    }

    fun clear() { _ids.value = emptySet() }
}
