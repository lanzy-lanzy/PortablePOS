package dev.ml.portablepos.presentation.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.usecase.SearchProductsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class POSUiState(
    val searchQuery: String = "",
    val searchResults: List<Product> = emptyList(),
    val isSearching: Boolean = false,
    val showCartExpanded: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class POSViewModel @Inject constructor(
    private val searchProductsUseCase: SearchProductsUseCase,
    val cartManager: CartManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(POSUiState())
    val uiState: StateFlow<POSUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                    } else {
                        searchProducts(query)
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query, isSearching = query.isNotBlank()) }
    }

    private fun searchProducts(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            searchProductsUseCase(query)
                .catch { e ->
                    _uiState.update { it.copy(isSearching = false, error = e.message ?: "Search failed") }
                }
                .collect { results ->
                    _uiState.update { it.copy(searchResults = results, isSearching = false) }
                }
        }
    }

    fun addToCart(product: Product) {
        cartManager.addItem(product)
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
        _searchQuery.value = ""
    }

    fun addBarcodeProduct(product: Product) {
        cartManager.addItem(product)
    }

    fun updateQuantity(productId: Long, quantity: Int) {
        cartManager.updateQuantity(productId, quantity)
    }

    fun removeFromCart(productId: Long) {
        cartManager.removeItem(productId)
    }

    fun toggleCartExpanded() {
        _uiState.update { it.copy(showCartExpanded = !it.showCartExpanded) }
    }

    fun clearCart() {
        cartManager.clear()
    }
}
