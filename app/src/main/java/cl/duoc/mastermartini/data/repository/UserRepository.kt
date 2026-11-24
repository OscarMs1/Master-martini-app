package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.local.UserDao
import cl.duoc.mastermartini.data.local.UserEntity
import cl.duoc.mastermartini.data.model.BusinessSize
import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.model.User
import cl.duoc.mastermartini.data.model.UserRole
import cl.duoc.mastermartini.data.remote.ApiClient
import cl.duoc.mastermartini.data.remote.ApiException
import cl.duoc.mastermartini.data.remote.LoginBody
import cl.duoc.mastermartini.data.remote.RefreshBody
import cl.duoc.mastermartini.data.remote.RegistroBody
import cl.duoc.mastermartini.data.remote.TokenStorage
import cl.duoc.mastermartini.data.remote.UsuarioDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data object EmailTaken : AuthResult
    data object InvalidCredentials : AuthResult
    /** Error del backend o de conexión, con el mensaje para mostrar y los campos con problemas. */
    data class Error(val message: String, val fields: Map<String, String> = emptyMap()) : AuthResult
}

data class NewUser(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val run: String,
    val region: String,
    val comuna: String,
    val address: String,
    val businessType: BusinessType,
    val businessSize: BusinessSize,
    val interests: List<String>,
    val acceptedTerms: Boolean,
)

/**
 * Cuentas de usuario contra el backend de Master Martini (las mismas de la página web).
 * - El backend valida la contraseña y entrega un JWT; la app nunca guarda la contraseña.
 * - Room guarda una copia del perfil para mostrarlo sin conexión, más las preferencias propias
 *   de la app (tipo de negocio, tamaño e intereses).
 */
class UserRepository(
    private val userDao: UserDao,
    private val settings: SettingsDataStore,
    private val client: ApiClient,
    private val tokens: TokenStorage,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<User?> = settings.settings
        .map { it.loggedUserId }
        .flatMapLatest { id -> if (id == null) flowOf(null) else userDao.observeById(id).map { it?.toUser() } }

    suspend fun currentUserId(): Long? = settings.settings.first().loggedUserId

    suspend fun register(data: NewUser): AuthResult {
        val email = data.email.trim().lowercase()
        try {
            client.llamar {
                registro(
                    RegistroBody(
                        nombre = data.name.trim(), email = email, password = data.password,
                        run = data.run.trim(), telefono = data.phone.trim(),
                        region = data.region, comuna = data.comuna, direccion = data.address.trim(),
                    )
                )
            }
        } catch (e: ApiException) {
            return if (e.status == 409) AuthResult.EmailTaken else AuthResult.Error(e.message, e.fields)
        }
        // La cuenta quedó creada: iniciamos sesión con ella y guardamos las preferencias de la app.
        return login(email, data.password, Preferencias(data.businessType, data.businessSize, data.interests, data.acceptedTerms))
    }

    suspend fun login(email: String, password: String): AuthResult = login(email, password, null)

    private suspend fun login(email: String, password: String, prefs: Preferencias?): AuthResult {
        val token = try {
            client.llamar { login(LoginBody(email.trim().lowercase(), password)) }
        } catch (e: ApiException) {
            return if (e.status == 401) AuthResult.InvalidCredentials else AuthResult.Error(e.message)
        }
        tokens.save(token.accessToken, token.refreshToken)
        val user = guardar(token.usuario, prefs)
        settings.setLoggedUser(user.id)
        return AuthResult.Success(user.toUser())
    }

    /** Actualiza la copia local con los datos del backend (por ejemplo, si cambió el rol). */
    suspend fun refreshProfile() {
        if (currentUserId() == null) return
        runCatching { client.llamar { yo() } }.onSuccess { guardar(it, null) }
    }

    /**
     * Nombre y teléfono pertenecen a la cuenta del backend, que no tiene una ruta para editarlos:
     * desde la app solo se cambian las preferencias propias de la app.
     */
    suspend fun updateProfile(businessType: BusinessType, businessSize: BusinessSize, interests: List<String>) {
        val id = currentUserId() ?: return
        val current = userDao.findById(id) ?: return
        userDao.update(
            current.copy(
                businessType = businessType.name,
                businessSize = businessSize.name,
                interests = interests.joinToString("|"),
            )
        )
    }

    suspend fun logout() {
        val refresh = tokens.refreshToken()
        // Se revoca el refresh token en el servidor; si no hay conexión, igual se cierra la sesión local.
        if (refresh != null) runCatching { client.llamar { logout(RefreshBody(refresh)) } }
        tokens.clear()
        settings.setLoggedUser(null)
    }

    /** La sesión venció en el servidor (refresh token revocado o expirado). */
    suspend fun onSessionExpired() {
        tokens.clear()
        settings.setLoggedUser(null)
    }

    /** Hay un usuario marcado como conectado pero sin token guardado (por ejemplo, versión antigua). */
    suspend fun hasBrokenSession(): Boolean = currentUserId() != null && tokens.refreshToken() == null

    private data class Preferencias(
        val businessType: BusinessType,
        val businessSize: BusinessSize,
        val interests: List<String>,
        val acceptedTerms: Boolean,
    )

    private suspend fun guardar(dto: UsuarioDto, prefs: Preferencias?): UserEntity {
        val previo = userDao.findById(dto.id)
        val entity = UserEntity(
            id = dto.id,
            name = dto.nombre,
            email = dto.email,
            phone = dto.telefono.orEmpty(),
            role = dto.rol,
            run = dto.run.orEmpty(),
            region = dto.region.orEmpty(),
            comuna = dto.comuna.orEmpty(),
            address = dto.direccion.orEmpty(),
            businessType = prefs?.businessType?.name ?: previo?.businessType ?: BusinessType.PASTELERIA.name,
            businessSize = prefs?.businessSize?.name ?: previo?.businessSize ?: BusinessSize.PEQUENO.name,
            interests = prefs?.interests?.joinToString("|") ?: previo?.interests.orEmpty(),
            acceptedTerms = prefs?.acceptedTerms ?: previo?.acceptedTerms ?: true,
        )
        userDao.upsert(entity)
        return entity
    }
}

fun UserEntity.toUser() = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    businessType = BusinessType.fromName(businessType) ?: BusinessType.PASTELERIA,
    businessSize = BusinessSize.fromName(businessSize) ?: BusinessSize.PEQUENO,
    interests = interests.split("|").filter { it.isNotBlank() },
    // El rol lo decide el backend: la app no puede darse permisos de administrador.
    role = if (role == "ADMINISTRADOR") UserRole.ADMIN else UserRole.CLIENTE,
    run = run,
    region = region,
    comuna = comuna,
    address = address,
)
