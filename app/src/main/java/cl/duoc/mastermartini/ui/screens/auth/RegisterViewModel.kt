package cl.duoc.mastermartini.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.local.RegionCatalog
import cl.duoc.mastermartini.data.model.BusinessSize
import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.repository.AuthResult
import cl.duoc.mastermartini.data.repository.NewUser
import cl.duoc.mastermartini.data.repository.UserRepository
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: FieldState = FieldState(),
    val email: FieldState = FieldState(),
    val phone: FieldState = FieldState(),
    val run: FieldState = FieldState(),
    val region: FieldState = FieldState(),
    val comuna: FieldState = FieldState(),
    val address: FieldState = FieldState(),
    val password: FieldState = FieldState(),
    val confirm: FieldState = FieldState(),
    val businessType: BusinessType? = null,
    val businessTypeError: String? = null,
    val businessSize: BusinessSize? = null,
    val businessSizeError: String? = null,
    val interests: Set<String> = emptySet(),
    val interestsError: String? = null,
    val accepted: Boolean = false,
    val acceptedError: String? = null,
    val loading: Boolean = false,
    /** Error general del backend (por ejemplo, sin conexión). */
    val formError: String? = null,
    /** Aumenta en cada intento fallido de envío → dispara la animación de sacudida. */
    val failedAttempts: Int = 0,
) {
    /** Progreso del formulario (0..1) para la barra superior. */
    val progress: Float
        get() {
            val checks = listOf(
                name.value.isNotBlank() && Validators.name(name.value).isValid,
                Validators.email(email.value).isValid,
                Validators.phone(phone.value).isValid,
                Validators.run(run.value).isValid,
                region.value.isNotBlank() && region.error == null,
                comuna.value.isNotBlank() && comuna.error == null,
                Validators.address(address.value).isValid,
                Validators.password(password.value).isValid,
                confirm.value.isNotEmpty() && confirm.value == password.value,
                businessType != null,
                businessSize != null,
                interests.isNotEmpty(),
                accepted,
            )
            return checks.count { it } / checks.size.toFloat()
        }
}

sealed interface RegisterEvent {
    data object Registered : RegisterEvent
}

/**
 * ViewModel del registro. Toda la lógica de validación vive aquí (usando [Validators]);
 * la pantalla solo dibuja el [RegisterUiState] y reenvía eventos del usuario.
 */
class RegisterViewModel(private val users: UserRepository, val regions: RegionCatalog) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onNameChange(v: String) = _state.update { it.copy(name = it.name.update(v, Validators::name)) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = it.email.update(v, Validators::email)) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = it.phone.update(v, Validators::phone)) }
    fun onRunChange(v: String) = _state.update { it.copy(run = it.run.update(v.take(12), Validators::run)) }
    fun onRunBlur() = _state.update { it.copy(run = it.run.touch(Validators::run)) }
    fun onRegionChange(v: String) = _state.update {
        val comuna = if (regions.comunas(v).contains(it.comuna.value)) it.comuna else FieldState()
        it.copy(region = it.region.update(v, ::validateRegion).copy(touched = true), comuna = comuna)
    }
    fun onComunaChange(v: String) = _state.update {
        it.copy(comuna = it.comuna.update(v) { c -> validateComuna(it.region.value, c) }.copy(touched = true))
    }
    fun onAddressChange(v: String) = _state.update { it.copy(address = it.address.update(v, Validators::address)) }
    fun onAddressBlur() = _state.update { it.copy(address = it.address.touch(Validators::address)) }

    fun onPasswordChange(v: String) = _state.update {
        it.copy(
            password = it.password.update(v, Validators::password),
            // si ya escribió la confirmación, la revalidamos contra la nueva contraseña
            confirm = it.confirm.update(it.confirm.value) { c -> Validators.passwordConfirmation(v, c) },
        )
    }
    fun onConfirmChange(v: String) = _state.update {
        it.copy(confirm = it.confirm.update(v) { c -> Validators.passwordConfirmation(it.password.value, c) })
    }

    fun onNameBlur() = _state.update { it.copy(name = it.name.touch(Validators::name)) }
    fun onEmailBlur() = _state.update { it.copy(email = it.email.touch(Validators::email)) }
    fun onPhoneBlur() = _state.update { it.copy(phone = it.phone.touch(Validators::phone)) }
    fun onPasswordBlur() = _state.update { it.copy(password = it.password.touch(Validators::password)) }
    fun onConfirmBlur() = _state.update {
        it.copy(confirm = it.confirm.touch { c -> Validators.passwordConfirmation(it.password.value, c) })
    }

    fun onBusinessType(type: BusinessType) = _state.update { it.copy(businessType = type, businessTypeError = null) }
    fun onBusinessSize(size: BusinessSize) = _state.update { it.copy(businessSize = size, businessSizeError = null) }
    fun onToggleInterest(interest: String) = _state.update {
        val set = if (interest in it.interests) it.interests - interest else it.interests + interest
        it.copy(interests = set, interestsError = if (it.interestsError != null) Validators.atLeast(set, 1, MSG_INTEREST).error else null)
    }
    fun onAcceptedChange(v: Boolean) = _state.update {
        it.copy(accepted = v, acceptedError = if (it.acceptedError != null) Validators.acceptedTerms(v).error else null)
    }

    fun submit() {
        val s = _state.value
        val validated = s.copy(
            name = s.name.touch(Validators::name),
            email = s.email.touch(Validators::email),
            phone = s.phone.touch(Validators::phone),
            run = s.run.touch(Validators::run),
            region = s.region.touch(::validateRegion),
            comuna = s.comuna.touch { validateComuna(s.region.value, it) },
            address = s.address.touch(Validators::address),
            password = s.password.touch(Validators::password),
            confirm = s.confirm.touch { Validators.passwordConfirmation(s.password.value, it) },
            businessTypeError = Validators.selected(s.businessType, "Elige el tipo de negocio").error,
            businessSizeError = Validators.selected(s.businessSize, "Elige el tamaño").error,
            interestsError = Validators.atLeast(s.interests, 1, MSG_INTEREST).error,
            acceptedError = Validators.acceptedTerms(s.accepted).error,
        )
        val hasErrors = listOf(
            validated.name, validated.email, validated.phone, validated.run, validated.region,
            validated.comuna, validated.address, validated.password, validated.confirm,
        )
            .any { it.error != null } ||
            listOf(validated.businessTypeError, validated.businessSizeError, validated.interestsError, validated.acceptedError)
                .any { it != null }

        if (hasErrors) {
            _state.value = validated.copy(failedAttempts = s.failedAttempts + 1)
            return
        }

        _state.value = validated.copy(loading = true, formError = null)
        viewModelScope.launch {
            val result = users.register(
                NewUser(
                    name = s.name.value, email = s.email.value, phone = s.phone.value,
                    password = s.password.value, run = s.run.value, region = s.region.value,
                    comuna = s.comuna.value, address = s.address.value, businessType = s.businessType!!,
                    businessSize = s.businessSize!!, interests = s.interests.toList(),
                    acceptedTerms = s.accepted,
                )
            )
            when (result) {
                is AuthResult.Success -> _events.send(RegisterEvent.Registered)
                AuthResult.EmailTaken -> _state.update {
                    it.copy(email = it.email.copy(error = "Este correo ya tiene una cuenta"), failedAttempts = it.failedAttempts + 1)
                }
                AuthResult.InvalidCredentials -> Unit
                is AuthResult.Error -> _state.update { st ->
                    // Errores por campo que devuelve el backend (ej: "run", "telefono").
                    fun FieldState.with(key: String) = result.fields[key]?.let { copy(error = it, touched = true) } ?: this
                    st.copy(
                        name = st.name.with("nombre"), email = st.email.with("email"), phone = st.phone.with("telefono"),
                        run = st.run.with("run"), region = st.region.with("region"), comuna = st.comuna.with("comuna"),
                        address = st.address.with("direccion"), password = st.password.with("password"),
                        formError = result.message, failedAttempts = st.failedAttempts + 1,
                    )
                }
            }
            _state.update { it.copy(loading = false) }
        }
    }

    private fun validateRegion(v: String) = Validators.oneOf(v, regions.names, "Elige una región")
    private fun validateComuna(region: String, v: String) =
        Validators.oneOf(v, regions.comunas(region), if (region.isBlank()) "Primero elige la región" else "Elige una comuna")

    private companion object {
        const val MSG_INTEREST = "Elige al menos un tema"
    }
}
