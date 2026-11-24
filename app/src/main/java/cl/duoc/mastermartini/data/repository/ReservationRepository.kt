package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.local.ReservationDao
import cl.duoc.mastermartini.data.local.ReservationEntity
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.model.Reservation
import cl.duoc.mastermartini.data.model.User
import cl.duoc.mastermartini.data.remote.ApiClient
import cl.duoc.mastermartini.data.remote.ApiException
import cl.duoc.mastermartini.data.remote.ContactoBody
import cl.duoc.mastermartini.data.remote.InscripcionBody
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

sealed interface ReserveResult {
    data object Success : ReserveResult
    data object AlreadyReserved : ReserveResult
    data object NotLogged : ReserveResult
    data class Error(val message: String) : ReserveResult
}

/**
 * - Inscripciones a clases: en el backend (POST /api/v1/inscripciones), las mismas de la web.
 * - Asesorías: se envían como solicitud de contacto al backend y se guarda una copia en Room
 *   para mostrarlas en "Mis reservas".
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReservationRepository(
    private val dao: ReservationDao,
    private val settings: SettingsDataStore,
    private val client: ApiClient,
) {
    /** Inscripciones del backend, junto al usuario al que pertenecen. */
    private val remote = MutableStateFlow<Pair<Long, List<Reservation>>?>(null)

    val reservations: Flow<List<Reservation>> = settings.settings
        .map { it.loggedUserId }
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else combine(dao.observeAll(id), remote) { local, enrolled ->
                val classes = enrolled?.takeIf { it.first == id }?.second.orEmpty()
                classes + local.map { Reservation(it.id, it.serviceId, it.title, it.dateLabel, it.modality, it.createdAt) }
            }
        }

    private suspend fun userId(): Long? = settings.settings.first().loggedUserId

    suspend fun refreshEnrollments() {
        val id = userId() ?: return
        val list = runCatching { client.llamar { misInscripciones() } }.getOrNull() ?: return
        remote.value = id to list.map {
            Reservation(
                // Ids negativos para no chocar con las reservas locales de Room.
                id = -it.id,
                serviceId = ClassRepository.serviceId(it.claseId),
                title = it.titulo,
                dateLabel = ClassRepository.formatFecha(it.fechaClase).orEmpty(),
                modality = it.modalidad.orEmpty(),
                createdAt = 0,
                canCancel = false,
            )
        }
    }

    /** Inscribe al usuario en una clase del backend. */
    suspend fun enroll(serviceId: String): ReserveResult {
        userId() ?: return ReserveResult.NotLogged
        val claseId = ClassRepository.claseId(serviceId) ?: return ReserveResult.Error("Esta actividad no admite inscripción.")
        return try {
            client.llamar { inscribir(InscripcionBody(claseId)) }
            refreshEnrollments()
            ReserveResult.Success
        } catch (e: ApiException) {
            if (e.status == 409 && e.message.contains("inscrit", ignoreCase = true)) ReserveResult.AlreadyReserved
            else ReserveResult.Error(e.message)
        }
    }

    /** Envía la solicitud de asesoría al equipo (formulario de contacto) y la guarda en el teléfono. */
    suspend fun requestAdvisory(
        user: User,
        serviceId: String,
        title: String,
        dateLabel: String,
        modality: String,
        notes: String,
    ): ReserveResult {
        val id = userId() ?: return ReserveResult.NotLogged
        val mensaje = "Solicitud de asesoría desde la app.\n" +
            "Tema: $title\nFecha preferida: $dateLabel\nModalidad: $modality\nTeléfono: ${user.phone}\n\n$notes"
        try {
            client.llamar { contacto(ContactoBody(user.name, user.email, "Asesoría técnica", mensaje.take(2000))) }
        } catch (e: ApiException) {
            return ReserveResult.Error(e.message)
        }
        dao.insert(ReservationEntity(userId = id, serviceId = serviceId, title = title, dateLabel = dateLabel, modality = modality, notes = notes))
        return ReserveResult.Success
    }

    /** Solo se cancelan las copias locales de asesorías; las inscripciones a clases no se cancelan desde la app. */
    suspend fun cancel(id: Long) {
        if (id > 0) dao.delete(id)
    }
}
