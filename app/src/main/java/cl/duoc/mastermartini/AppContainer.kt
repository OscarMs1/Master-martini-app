package cl.duoc.mastermartini

import android.content.Context
import cl.duoc.mastermartini.data.local.AppDatabase
import cl.duoc.mastermartini.data.local.RegionCatalog
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.remote.ApiClient
import cl.duoc.mastermartini.data.remote.TokenStore
import cl.duoc.mastermartini.data.repository.ClassRepository
import cl.duoc.mastermartini.data.repository.ContentRepository
import cl.duoc.mastermartini.data.repository.FakeContentRepository
import cl.duoc.mastermartini.data.repository.ProductRepository
import cl.duoc.mastermartini.data.repository.QuoteRepository
import cl.duoc.mastermartini.data.repository.ReservationRepository
import cl.duoc.mastermartini.data.repository.UserRepository
import cl.duoc.mastermartini.device.ContactHelper
import cl.duoc.mastermartini.device.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Inyección de dependencias manual: crea una sola vez cada repositorio
 * y los comparte con los ViewModels (ver ui/AppViewModelProvider).
 *
 * La app se conecta al backend de Master Martini en [BuildConfig.API_BASE_URL]
 * (por defecto http://10.0.2.2:8095/, que es el PC visto desde el emulador de Android).
 */
class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)
    /** Tareas que no dependen de una pantalla (cerrar la sesión vencida, actualizar el perfil). */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val settings = SettingsDataStore(context)
    val regions = RegionCatalog.load(context)

    private val tokens = TokenStore(context)
    val api = ApiClient(BuildConfig.API_BASE_URL, tokens, onSessionExpired = {
        appScope.launch { users.onSessionExpired() }
    })

    /** Contenido de "Aprende", cursos y asesorías: sigue siendo local (MVP académico). */
    val content: ContentRepository = FakeContentRepository()
    val users: UserRepository = UserRepository(db.userDao(), settings, api, tokens)
    val products = ProductRepository(api)
    val classes = ClassRepository(api)
    val quotes = QuoteRepository(db.quoteDao(), settings, api)
    val reservations = ReservationRepository(db.reservationDao(), settings, api)

    val notifications = NotificationHelper(context)
    val contact = ContactHelper

    init {
        appScope.launch {
            // Sesión guardada por una versión anterior de la app (sin token): se pide iniciar sesión de nuevo.
            if (users.hasBrokenSession()) users.logout() else users.refreshProfile()
        }
    }
}
