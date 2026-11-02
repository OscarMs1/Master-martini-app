package cl.duoc.mastermartini.data.repository

import android.content.Context
import cl.duoc.mastermartini.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Catálogo editable de demostración. Se guarda solo en este dispositivo.
 * No es un backend, ni un sistema de permisos para empleados de Master Martini.
 */
class LocalProductRepository(context: Context, private val initialProducts: List<Product>) {
    private val preferences = context.getSharedPreferences("academic_catalog_editor", Context.MODE_PRIVATE)
    private val key = "products_v1"

    private val state = MutableStateFlow(read())
    val products: StateFlow<List<Product>> = state

    fun save(product: Product) {
        require(product.name.isNotBlank() && product.formats.isNotEmpty())
        val updated = state.value.toMutableList()
        val index = updated.indexOfFirst { it.id == product.id }
        if (index < 0) updated.add(product) else updated[index] = product
        persist(updated)
    }

    fun delete(id: String) = persist(state.value.filterNot { it.id == id })

    fun restoreExamples() = persist(initialProducts)

    private fun read(): List<Product> {
      return try {
        val raw = preferences.getString(key, null) ?: return initialProducts
        val list = JSONArray(raw)
        (0 until list.length()).map { i ->
            val obj = list.getJSONObject(i)
            Product(
                id = obj.getString("id"), name = obj.getString("name"),
                category = obj.getString("category"),
                formats = obj.getJSONArray("formats").let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                },
                description = obj.getString("description"),
                imagePath = obj.optString("imagePath").takeIf { it.isNotBlank() },
            )
        }
    } catch (_: Exception) {
        initialProducts
    }
    }

    private fun persist(products: List<Product>) {
        val data = JSONArray()
        products.forEach { product ->
            val formats = JSONArray().apply { product.formats.forEach { put(it) } }
            data.put(JSONObject().apply {
                put("id", product.id)
                put("name", product.name)
                put("category", product.category)
                put("formats", formats)
                put("description", product.description)
                put("imagePath", product.imagePath ?: "")
            })
        }
        if (preferences.edit().putString(key, data.toString()).commit()) state.value = products
    }
}
