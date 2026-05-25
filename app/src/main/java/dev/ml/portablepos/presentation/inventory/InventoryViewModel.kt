package dev.ml.portablepos.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InventoryFilter { ALL, LOW_STOCK, OUT_OF_STOCK }

data class InventoryUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: InventoryFilter = InventoryFilter.ALL,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect {
                    applyFiltersAndSearch()
                }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            productRepository.getAllProducts()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load products") }
                }
                .collect { products ->
                    _uiState.update {
                        it.copy(products = products, isLoading = false)
                    }
                    applyFiltersAndSearch()
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilter(filter: InventoryFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFiltersAndSearch()
    }

    private fun applyFiltersAndSearch() {
        val state = _uiState.value
        var filtered = state.products

        filtered = when (state.selectedFilter) {
            InventoryFilter.ALL -> filtered
            InventoryFilter.LOW_STOCK -> filtered.filter { it.stockQuantity > 0 && it.stockQuantity <= it.reorderLevel }
            InventoryFilter.OUT_OF_STOCK -> filtered.filter { it.stockQuantity <= 0 }
        }

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                (it.barcode?.lowercase()?.contains(query) == true)
            }
        }

        _uiState.update { it.copy(filteredProducts = filtered) }
    }

    fun retry() {
        loadProducts()
    }
}
