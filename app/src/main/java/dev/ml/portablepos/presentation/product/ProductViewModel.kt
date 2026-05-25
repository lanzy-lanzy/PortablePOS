package dev.ml.portablepos.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.Category
import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.CategoryRepository
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.usecase.DeleteProductUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductListUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    init {
        loadProducts()
        loadCategories()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            combine(
                _refreshTrigger,
                _searchQuery,
                _selectedCategoryId,
                productRepository.getAllProducts(),
                categoryRepository.getAllCategories()
            ) { _, query, categoryId, products, categories ->
                var filtered = products
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.barcode?.contains(query, ignoreCase = true) == true
                    }
                }
                if (categoryId != null) {
                    filtered = filtered.filter { it.categoryId == categoryId }
                }
                _uiState.value = ProductListUiState(
                    isLoading = false,
                    products = filtered,
                    categories = categories,
                    selectedCategoryId = categoryId,
                    searchQuery = query,
                    error = null
                )
            }.collect { }
        }
    }

    fun refreshProducts() {
        _refreshTrigger.value = System.currentTimeMillis()
        _uiState.update { it.copy(isLoading = true) }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .catch { }
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            deleteProductUseCase(product)
        }
    }
}
