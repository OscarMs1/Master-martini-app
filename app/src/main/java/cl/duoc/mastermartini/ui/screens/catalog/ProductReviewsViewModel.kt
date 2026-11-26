package cl.duoc.mastermartini.ui.screens.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.remote.ApiException
import cl.duoc.mastermartini.data.repository.ProductRepository
import cl.duoc.mastermartini.data.repository.ProductReviews
import cl.duoc.mastermartini.data.repository.Review
import cl.duoc.mastermartini.data.repository.ReviewRepository
import cl.duoc.mastermartini.data.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewsUiState(
    val productName: String = "",
    val loading: Boolean = true,
    val error: String? = null,
    val data: ProductReviews? = null,
    val logged: Boolean = false,
    val rating: Int = 0,
    val html: String = "",
    val textLength: Int = 0,
    val sending: Boolean = false,
    /** Sube cada vez que la pantalla debe cargar [editorPreset] en el editor (vaciarlo o editar la propia). */
    val editorVersion: Int = 0,
    val editorPreset: String = "",
    val showErrors: Boolean = false,
    val confirmingDelete: Review? = null,
) {
    val ratingError: String? get() = if (rating !in 1..5) "Elige de 1 a 5 estrellas." else null
    val textError: String? get() = when {
        textLength < ReviewRepository.MIN_CHARS -> "Escribe al menos ${ReviewRepository.MIN_CHARS} caracteres."
        textLength > ReviewRepository.MAX_CHARS -> "Máximo ${ReviewRepository.MAX_CHARS} caracteres."
        else -> null
    }
    val editingOwn: Boolean get() = data?.mine != null
}

/** Opiniones de un producto: lista, promedio y el formulario para publicar la propia (con TipTap). */
class ProductReviewsViewModel(
    savedState: SavedStateHandle,
    products: ProductRepository,
    private val users: UserRepository,
    private val reviews: ReviewRepository,
) : ViewModel() {
    private val productId: String = checkNotNull(savedState["id"])

    private val _state = MutableStateFlow(
        ReviewsUiState(productName = products.products.value.firstOrNull { it.id == productId }?.name.orEmpty())
    )
    val state: StateFlow<ReviewsUiState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init {
        viewModelScope.launch { users.currentUser.collect { u -> _state.update { it.copy(logged = u != null) } } }
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val data = reviews.load(productId)
                _state.update { it.copy(loading = false, data = data) }
            } catch (e: ApiException) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun onRating(value: Int) = _state.update { it.copy(rating = value) }

    fun onEditorChange(html: String, textLength: Int) = _state.update { it.copy(html = html, textLength = textLength) }

    /** Carga la opinión propia en el formulario para cambiarla. */
    fun editMine() {
        val mine = _state.value.data?.mine ?: return
        _state.update { it.copy(rating = mine.rating, editorPreset = mine.html, editorVersion = it.editorVersion + 1) }
    }

    fun publish() {
        val s = _state.value
        if (s.ratingError != null || s.textError != null) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(sending = true) }
            try {
                reviews.publish(productId, s.rating, s.html)
                val data = reviews.load(productId)
                _state.update {
                    it.copy(
                        sending = false, data = data, rating = 0, html = "", textLength = 0, showErrors = false,
                        editorPreset = "", editorVersion = it.editorVersion + 1,
                    )
                }
                _messages.send(if (s.editingOwn) "Actualizamos tu opinión." else "¡Gracias! Publicamos tu opinión.")
            } catch (e: ApiException) {
                _state.update { it.copy(sending = false) }
                _messages.send(e.message)
            }
        }
    }

    fun askDelete(review: Review) = _state.update { it.copy(confirmingDelete = review) }
    fun dismissDelete() = _state.update { it.copy(confirmingDelete = null) }

    fun confirmDelete() {
        val review = _state.value.confirmingDelete ?: return
        _state.update { it.copy(confirmingDelete = null) }
        viewModelScope.launch {
            try {
                reviews.delete(review.id)
                _state.update { it.copy(data = reviews.load(productId)) }
                _messages.send("Borramos tu opinión.")
            } catch (e: ApiException) {
                _messages.send(e.message)
            }
        }
    }
}
