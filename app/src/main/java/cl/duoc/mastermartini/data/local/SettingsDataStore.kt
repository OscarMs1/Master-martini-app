package cl.duoc.mastermartini.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "mm_settings")

data class AppSettings(
    val loggedUserId: Long? = null,
    val largeText: Boolean = false,
    val highContrast: Boolean = false,
)

/**
 * Preferencias livianas (clave-valor) con DataStore:
 * sesión activa y opciones de accesibilidad de "Mi cuenta".
 */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val USER_ID = longPreferencesKey("logged_user_id")
        val LARGE_TEXT = booleanPreferencesKey("large_text")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            loggedUserId = p[Keys.USER_ID],
            largeText = p[Keys.LARGE_TEXT] ?: false,
            highContrast = p[Keys.HIGH_CONTRAST] ?: false,
        )
    }

    val learning: Flow<LearningState> = context.dataStore.data.map { p ->
        val id = p[Keys.USER_ID]
        if (id == null) LearningState() else LearningState(
            p[stringSetPreferencesKey("favorites_$id")] ?: emptySet(),
            p[stringSetPreferencesKey("modules_$id")] ?: emptySet(),
        )
    }

    suspend fun toggleLearning(value: String, module: Boolean = false) {
        context.dataStore.edit { p ->
            val id = p[Keys.USER_ID] ?: return@edit
            val key = stringSetPreferencesKey("${if (module) "modules" else "favorites"}_$id")
            val old = p[key] ?: emptySet()
            p[key] = if (value in old) old - value else old + value
        }
    }

    suspend fun setLoggedUser(id: Long?) {
        context.dataStore.edit { p ->
            if (id == null) p.remove(Keys.USER_ID) else p[Keys.USER_ID] = id
        }
    }

    suspend fun setLargeText(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LARGE_TEXT] = enabled }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }
}
