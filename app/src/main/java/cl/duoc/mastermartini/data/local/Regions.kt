package cl.duoc.mastermartini.data.local

import android.content.Context
import com.google.gson.Gson

data class Region(val region: String, val comunas: List<String>)

/**
 * Regiones y comunas donde despacha Master Martini. Es el mismo archivo que usa el backend
 * (regiones-comunas.json), así la app solo ofrece valores que el backend acepta.
 */
class RegionCatalog(val regions: List<Region>) {
    val names: List<String> get() = regions.map { it.region }
    fun comunas(region: String?): List<String> = regions.firstOrNull { it.region == region }?.comunas.orEmpty()

    companion object {
        fun load(context: Context): RegionCatalog = RegionCatalog(
            runCatching {
                context.assets.open("regiones-comunas.json").bufferedReader().use {
                    Gson().fromJson(it, Array<Region>::class.java).toList()
                }
            }.getOrDefault(emptyList())
        )
    }
}
