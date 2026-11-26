package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.model.ServiceItem
import cl.duoc.mastermartini.data.model.ServiceType
import cl.duoc.mastermartini.data.remote.ApiClient
import cl.duoc.mastermartini.data.remote.ApiException
import cl.duoc.mastermartini.data.remote.ClaseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Clases y talleres de Master Martini (GET /api/v1/clases), los mismos de la página web. */
class ClassRepository(private val client: ApiClient) {
    private val _classes = MutableStateFlow<List<ServiceItem>>(emptyList())
    val classes: StateFlow<List<ServiceItem>> = _classes.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun refresh() {
        try {
            // Solo clases futuras: las pasadas ya no aceptan inscripciones.
            _classes.value = client.llamar { clases() }.filter { !yaPaso(it.fecha) }.map { it.toService() }
            _error.value = null
        } catch (e: ApiException) {
            _error.value = e.message
        }
    }

    companion object {
        const val PREFIX = "clase-"

        fun serviceId(claseId: Long) = "$PREFIX$claseId"
        fun claseId(serviceId: String): Long? = serviceId.removePrefix(PREFIX).takeIf { serviceId.startsWith(PREFIX) }?.toLongOrNull()

        private val FORMATO = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy · HH:mm 'h'", Locale("es", "CL"))

        private val CHILE = ZoneId.of("America/Santiago")

        /** El backend envía la fecha en ISO 8601 (con o sin zona horaria). */
        private fun parse(iso: String?): LocalDateTime? = if (iso.isNullOrBlank()) null else
            runCatching { OffsetDateTime.parse(iso).atZoneSameInstant(CHILE).toLocalDateTime() }
                .recoverCatching { LocalDateTime.parse(iso) }
                .getOrNull()

        fun formatFecha(iso: String?): String? {
            if (iso.isNullOrBlank()) return null
            return parse(iso)?.format(FORMATO) ?: iso
        }

        fun yaPaso(iso: String?, ahora: LocalDateTime = LocalDateTime.now(CHILE)): Boolean =
            parse(iso)?.isBefore(ahora) ?: false
    }

    private fun ClaseDto.toService() = ServiceItem(
        id = serviceId(id),
        type = ServiceType.EVENT,
        title = titulo,
        badge = (modalidad ?: "Clase").uppercase(Locale("es", "CL")),
        detail = listOfNotNull(lugar, nivel, duracion).filter { it.isNotBlank() }.joinToString(" · "),
        dateLabel = formatFecha(fecha),
        takenSeats = 0,
        totalSeats = cuposDisponibles,
        productIds = productos.orEmpty().map { it.id.toString() },
        description = descripcion,
    )
}
