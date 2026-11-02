package cl.duoc.mastermartini.ui.screens.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.local.LearningState
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.data.model.LearnKind
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.repository.ContentRepository
import cl.duoc.mastermartini.domain.validation.LearnFilters
import java.text.Normalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LearnUiState(
    val query: String = "",
    val kind: LearnKind = LearnKind.VIDEO,
    val category: String? = null,
    val favoritesOnly: Boolean = false,
    val categories: List<String> = emptyList(),
    val items: List<LearnItem> = emptyList(),
    val suggestions: List<LearnItem> = emptyList(),
)

class LearnViewModel(
    savedState: SavedStateHandle,
    private val content: ContentRepository,
    private val settings: SettingsDataStore,
) : ViewModel() {
    private val initialKind = savedState.get<String>("tab")
        ?.let { requested -> LearnKind.entries.firstOrNull { it.name == requested } }
        ?: LearnKind.VIDEO

    private val initialCategory = savedState.get<String>("category")
        ?.takeIf { it in LearnFilters.categories(content.learnItems(), initialKind) }
    private val filters = MutableStateFlow(
        LearnUiState(kind = initialKind, category = initialCategory),
    )

    val state: StateFlow<LearnUiState> = combine(filters, settings.learning) { filter, learning ->
        val all = content.learnItems()
        val categories = LearnFilters.categories(all, filter.kind)
        val validCategory = filter.category?.takeIf { it in categories }
        val results = LearnFilters.filter(
            all, filter.kind, validCategory, filter.query,
            learning.favorites, filter.favoritesOnly,
        )
        val suggestions = if (!filter.favoritesOnly && results.size < 3 &&
            (validCategory != null || filter.query.isNotBlank())) {
            all.filter { it.kind == filter.kind && it.id !in results.map(LearnItem::id) }.take(3)
        } else emptyList()
        filter.copy(
            category = validCategory,
            categories = categories,
            items = results,
            suggestions = suggestions,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LearnUiState(kind = initialKind))

    fun onQuery(q: String) = filters.update { it.copy(query = q) }
    fun onKind(kind: LearnKind) = filters.update { it.copy(kind = kind, category = null) }
    fun onCategory(category: String?) = filters.update { it.copy(category = category) }
    fun toggleFavorites() = filters.update { it.copy(favoritesOnly = !it.favoritesOnly) }
    fun clearFilters() = filters.update { it.copy(query = "", category = null, favoritesOnly = false) }
}

/** Normalización de búsqueda compartida con el catálogo. */
internal fun String.normalized(): String = Normalizer
    .normalize(lowercase().trim(), Normalizer.Form.NFD)
    .replace(Regex("\\p{M}"), "")

data class LearnDetailUiState(
    val item: LearnItem? = null,
    val products: List<Product> = emptyList(),
)

class LearnDetailViewModel(
    savedState: SavedStateHandle,
    content: ContentRepository,
    private val settings: SettingsDataStore,
) : ViewModel() {
    val state: LearnDetailUiState = run {
        val item = savedState.get<String>("id")?.let(content::learnItem)
        LearnDetailUiState(item, item?.productIds?.mapNotNull(content::product).orEmpty())
    }

    val learning = settings.learning.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), LearningState(),
    )

    fun favorite() {
        state.item?.let { item -> viewModelScope.launch { settings.toggleLearning(item.id) } }
    }

    fun module(index: Int) {
        state.item?.let { item ->
            if (index in item.steps.indices) {
                viewModelScope.launch { settings.toggleLearning("${item.id}:$index", true) }
            }
        }
    }
}
