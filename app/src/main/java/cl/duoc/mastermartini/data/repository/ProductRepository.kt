package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.remote.ApiClient
import cl.duoc.mastermartini.data.remote.ApiException
import cl.duoc.mastermartini.data.remote.ProductoBody
import cl.duoc.mastermartini.data.remote.ProductoDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Categorías del backend (slug) y su nombre para mostrar. */
enum class ProductCategory(val slug: String, val label: String, val defaultImage: String) {
    PANADERIA("panaderia", "Panadería", "/assets/images/panaderia.jpg"),
    GELATERIA("gelateria", "Gelatería", "/assets/images/categoria-gelato.jpg"),
    FOOD_SERVICE("food-service", "Food service", "/assets/images/hero-panaderia.jpg"),
    RETAIL("retail", "Retail", "/assets/images/categoria-retail.jpg");

    companion object {
        fun fromSlug(slug: String) = entries.firstOrNull { it.slug == slug }
        fun fromLabel(label: String) = entries.firstOrNull { it.label == label }
    }
}

/** Datos que el administrador completa en el formulario de producto. */
data class ProductDraft(
    val name: String,
    val category: ProductCategory,
    val format: String,
    val description: String,
    val price: Long,
    val discount: Int,
    val stock: Int,
    val featured: Boolean,
)

data class CatalogStatus(val loading: Boolean = false, val error: String? = null)

/**
 * Catálogo real de Master Martini, el mismo de la página web (GET /api/v1/productos).
 * Crear, editar y eliminar requiere una cuenta ADMINISTRADOR: el backend lo comprueba.
 */
class ProductRepository(private val client: ApiClient) {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _status = MutableStateFlow(CatalogStatus())
    val status: StateFlow<CatalogStatus> = _status.asStateFlow()

    suspend fun refresh() {
        _status.value = _status.value.copy(loading = true)
        try {
            _products.value = client.llamar { productos() }.filterNot { it.retirado == true }.map { it.toProduct() }
            _status.value = CatalogStatus()
        } catch (e: ApiException) {
            _status.value = CatalogStatus(error = e.message)
        }
    }

    fun product(id: String): Product? = _products.value.firstOrNull { it.id == id }

    /** Sube la foto (si hay) y crea o actualiza el producto. Lanza [ApiException] si falla. */
    suspend fun save(existing: Product?, draft: ProductDraft, photoJpeg: ByteArray?) {
        val image = when {
            photoJpeg != null -> uploadImage(photoJpeg)
            existing?.serverImage != null -> existing.serverImage
            else -> draft.category.defaultImage
        }
        val body = ProductoBody(
            nombre = draft.name.trim(), categoria = draft.category.slug, formato = draft.format.trim(),
            descripcion = draft.description.trim(), precio = draft.price, descuento = draft.discount,
            stock = draft.stock, imagen = image, destacado = draft.featured,
        )
        val id = existing?.remoteId
        if (id == null) client.llamar { crearProducto(body) } else client.llamar { editarProducto(id, body) }
        refresh()
    }

    suspend fun delete(product: Product) {
        val id = product.remoteId ?: return
        client.llamar { eliminarProducto(id) }
        refresh()
    }

    private suspend fun uploadImage(jpeg: ByteArray): String {
        val part = MultipartBody.Part.createFormData(
            "archivo", "producto.jpg", jpeg.toRequestBody("image/jpeg".toMediaType()),
        )
        return client.llamar { subirImagen(part) }.url
    }

    private fun ProductoDto.toProduct() = Product(
        id = id.toString(),
        name = nombre,
        category = ProductCategory.fromSlug(categoria)?.label ?: categoria,
        formats = listOf(formato),
        description = descripcion,
        imagePath = client.urlImagen(imagen),
        price = precio,
        discount = descuento,
        stock = stock,
        featured = destacado,
        serverImage = imagen,
    )
}
