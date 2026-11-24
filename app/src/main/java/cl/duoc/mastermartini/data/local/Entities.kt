package cl.duoc.mastermartini.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tablas de Room (SQLite local). */

/**
 * Copia local del usuario que inició sesión. El [id] es el mismo del backend.
 * La contraseña ya no se guarda en el teléfono: la valida el backend.
 * Tipo de negocio, tamaño e intereses son preferencias de la app (el backend no las tiene).
 */
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    /** CLIENTE o ADMINISTRADOR, según el backend. */
    val role: String,
    val run: String,
    val region: String,
    val comuna: String,
    val address: String,
    val businessType: String,
    val businessSize: String,
    /** Intereses separados por "|" */
    val interests: String,
    val acceptedTerms: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Ítems de la cotización en curso (carrito). Uno por producto + formato. */
@Entity(tableName = "quote_items", primaryKeys = ["userId", "productId", "format"])
data class QuoteItemEntity(
    val userId: Long,
    val productId: String,
    val productName: String,
    val format: String,
    val quantity: Int,
)

/**
 * Pedidos enviados al backend. El [id] es el número de pedido del backend; aquí se guardan además
 * los datos que el backend no tiene (fecha de despacho deseada y notas).
 */
@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val id: Long,
    val userId: Long,
    val itemsSummary: String,
    val itemCount: Int,
    val contactName: String,
    val address: String,
    val comuna: String,
    val deliveryDate: String,
    val notes: String,
    /** APROBADO o RECHAZADO (pago simulado). */
    val status: String,
    val total: Long,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Inscripciones a eventos/cursos y reservas de asesorías. */
@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val serviceId: String,
    val title: String,
    val dateLabel: String,
    val modality: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
