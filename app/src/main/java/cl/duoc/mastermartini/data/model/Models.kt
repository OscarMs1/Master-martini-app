package cl.duoc.mastermartini.data.model

/**
 * Modelos de dominio de la app. Son clases simples (sin dependencias de Android)
 * que usan las pantallas. Cuentas, catálogo, pedidos y clases vienen del backend de
 * Master Martini; el contenido de "Aprende" sigue siendo local (MVP académico).
 */

enum class BusinessType(val label: String) {
    PASTELERIA("Pastelería"),
    PANADERIA("Panadería"),
    CHOCOLATERIA("Chocolatería"),
    HELADERIA("Heladería"),
    RESTAURANTE("Restaurante / Casino");

    companion object {
        fun fromName(name: String?): BusinessType? = entries.firstOrNull { it.name == name }
    }
}

enum class BusinessSize(val label: String) {
    PEQUENO("Pequeño"),
    MEDIANO("Mediano"),
    GRANDE("Grande");

    companion object {
        fun fromName(name: String?): BusinessSize? = entries.firstOrNull { it.name == name }
    }
}

/** Temas que el usuario puede marcar como interés al registrarse. */
val LEARNING_INTERESTS = listOf(
    "Chocolatería", "Pastelería de vitrina", "Masas de hoja", "Heladería", "Galletas", "Panadería"
)

/** Usuario ya autenticado (sin datos sensibles como el hash de la contraseña). */
enum class UserRole { CLIENTE, ADMIN }

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val businessType: BusinessType,
    val businessSize: BusinessSize,
    val interests: List<String>,
    val role: UserRole = UserRole.CLIENTE,
    /** Datos de la cuenta en el backend, necesarios para crear pedidos. */
    val run: String = "",
    val region: String = "",
    val comuna: String = "",
    val address: String = "",
) {
    val isAdmin: Boolean get() = role == UserRole.ADMIN
    val initials: String
        get() = name.trim().split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }
    val firstName: String get() = name.trim().split(" ").firstOrNull().orEmpty()
}

// ---------- Aprende ----------

enum class LearnKind(val label: String) { VIDEO("Técnicas"), RECIPE("Recetas"), COURSE("Cursos") }

enum class Difficulty(val label: String) { EASY("Fácil"), MEDIUM("Medio"), HARD("Avanzado") }

data class LearnItem(
    val id: String,
    val kind: LearnKind,
    val title: String,
    val category: String,
    val duration: String,
    val difficulty: Difficulty,
    val description: String,
    val steps: List<String>,
    /** Productos del catálogo relacionados con este contenido. */
    val productIds: List<String>,
    val ingredients: List<String> = emptyList(),
    val sourceUrl: String? = null,
    val author: String = "Contenido académico",
    val isNew: Boolean = false,
)

// ---------- Catálogo ----------

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val formats: List<String>,
    val description: String,
    /** Foto local (contenido de "Aprende") o ruta/URL de la foto en el servidor. */
    val imagePath: String? = null,
    /** Precio en pesos chilenos, sin descuento. 0 en los productos de ejemplo locales. */
    val price: Long = 0,
    /** Porcentaje de descuento (0 a 90). */
    val discount: Int = 0,
    val stock: Int = 0,
    val featured: Boolean = false,
    /** Ruta de la imagen tal como la guarda el backend (se reenvía al editar). */
    val serverImage: String? = null,
) {
    /** Mismo cálculo que el backend y la web: precio con descuento, redondeado. */
    val finalPrice: Long get() = Math.round(price * (100 - discount) / 100.0)
    val remoteId: Long? get() = id.toLongOrNull()
}

/** Formato chileno: $12.990 */
fun formatClp(value: Long): String = "$" + "%,d".format(value).replace(',', '.')

/** Producto agregado a la cotización en curso. */
data class QuoteLine(
    val productId: String,
    val productName: String,
    val format: String,
    val quantity: Int,
)

/** Pedido enviado al backend (la cotización se confirma como pedido con pago simulado). */
data class SentQuote(
    val id: Long,
    val code: String,
    val itemsSummary: String,
    val itemCount: Int,
    val address: String,
    val comuna: String,
    val deliveryDate: String,
    val createdAt: Long,
    /** APROBADO o RECHAZADO (pago simulado). */
    val status: String = "",
    val total: Long = 0,
)

// ---------- Servicios ----------

enum class ServiceType(val label: String) { COURSE("Cursos"), ADVISORY("Asesorías"), EVENT("Eventos") }

data class ServiceItem(
    val id: String,
    val type: ServiceType,
    val title: String,
    val badge: String,
    val detail: String,
    val dateLabel: String? = null,
    val takenSeats: Int = 0,
    val totalSeats: Int = 0,
    val progress: Float? = null,
    val hasCertificate: Boolean = false,
    /** Para cursos: id del contenido en "Aprende" que se abre al continuar. */
    val learnId: String? = null,
) {
    val availableSeats: Int get() = totalSeats - takenSeats
}

data class Reservation(
    val id: Long,
    val serviceId: String,
    val title: String,
    val dateLabel: String,
    val modality: String,
    val createdAt: Long,
    /** Las inscripciones a clases viven en el backend y no se cancelan desde la app. */
    val canCancel: Boolean = true,
)

