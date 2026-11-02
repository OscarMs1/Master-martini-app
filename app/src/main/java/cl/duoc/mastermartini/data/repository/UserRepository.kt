package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.local.UserDao
import cl.duoc.mastermartini.data.local.UserEntity
import cl.duoc.mastermartini.data.model.BusinessSize
import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.model.User
import cl.duoc.mastermartini.data.model.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data object EmailTaken : AuthResult
    data object InvalidCredentials : AuthResult
}

data class NewUser(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val businessType: BusinessType,
    val businessSize: BusinessSize,
    val interests: List<String>,
    val acceptedTerms: Boolean,
)

/** Registro, inicio de sesión y perfil. Une Room (usuarios) y DataStore (sesión). */
class UserRepository(
    private val userDao: UserDao,
    private val settings: SettingsDataStore,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<User?> = settings.settings
        .map { it.loggedUserId }
        .flatMapLatest { id -> if (id == null) flowOf(null) else userDao.observeById(id).map { it?.toUser() } }

    suspend fun currentUserId(): Long? = settings.settings.first().loggedUserId

    suspend fun register(data: NewUser): AuthResult {
        val email = data.email.trim().lowercase()
        if (DemoAdminAccount.isReservedEmail(email) || userDao.findByEmail(email) != null) return AuthResult.EmailTaken
        val salt = newSalt()
        val entity = UserEntity(
            name = data.name.trim(),
            email = email,
            phone = data.phone.filter { it.isDigit() || it == '+' },
            passwordHash = hash(data.password, salt),
            salt = salt,
            businessType = data.businessType.name,
            businessSize = data.businessSize.name,
            interests = data.interests.joinToString("|"),
            acceptedTerms = data.acceptedTerms,
        )
        val id = userDao.insert(entity)
        settings.setLoggedUser(id)
        return AuthResult.Success(entity.copy(id = id).toUser())
    }

    suspend fun login(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        // Cuenta predefinida para la EP2: se utiliza el mismo formulario que los clientes.
        // No se puede registrar otro ADMIN desde la app.
        if (DemoAdminAccount.isReservedEmail(normalizedEmail)) {
            if (!DemoAdminAccount.matches(normalizedEmail, password)) return AuthResult.InvalidCredentials
            val existing = userDao.findByEmail(DemoAdminAccount.EMAIL)
            val admin = if (existing == null) {
                val salt = DemoAdminAccount.SEEDED_SALT
                val created = UserEntity(
                    name = "Administrador", email = DemoAdminAccount.EMAIL,
                    phone = "+56911111111", passwordHash = hash(password, salt), salt = salt,
                    businessType = BusinessType.PASTELERIA.name,
                    businessSize = BusinessSize.PEQUENO.name,
                    interests = "", acceptedTerms = true,
                )
                val id = userDao.insert(created)
                created.copy(id = id)
            } else if (!DemoAdminAccount.isSeededAccount(existing.salt, existing.passwordHash)) {
                // Una cuenta antigua con el correo reservado no obtiene privilegios por su email.
                existing.copy(
                    name = "Administrador", salt = DemoAdminAccount.SEEDED_SALT,
                    passwordHash = DemoAdminAccount.SEEDED_HASH,
                ).also { userDao.update(it) }
            } else existing
            settings.setLoggedUser(admin.id)
            return AuthResult.Success(admin.toUser())
        }
        val user = userDao.findByEmail(normalizedEmail) ?: return AuthResult.InvalidCredentials
        if (hash(password, user.salt) != user.passwordHash) return AuthResult.InvalidCredentials
        settings.setLoggedUser(user.id)
        return AuthResult.Success(user.toUser())
    }

    suspend fun updateProfile(
        name: String,
        phone: String,
        businessType: BusinessType,
        businessSize: BusinessSize,
        interests: List<String>,
    ) {
        val id = currentUserId() ?: return
        val current = userDao.findById(id) ?: return
        userDao.update(
            current.copy(
                name = name.trim(),
                phone = phone.filter { it.isDigit() || it == '+' },
                businessType = businessType.name,
                businessSize = businessSize.name,
                interests = interests.joinToString("|"),
            )
        )
    }

    suspend fun logout() = settings.setLoggedUser(null)

    private fun newSalt(): String {
        val bytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((salt + password).toByteArray())
        return Base64.getEncoder().encodeToString(digest)
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
    role = if (DemoAdminAccount.isReservedEmail(email) &&
        DemoAdminAccount.isSeededAccount(salt, passwordHash)) UserRole.ADMIN else UserRole.CLIENTE,
)
