package dev.ml.portablepos.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.data.local.preferences.AppPreferences
import dev.ml.portablepos.domain.model.Category
import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.CategoryRepository
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.usecase.AddProductUseCase
import dev.ml.portablepos.domain.usecase.UpdateProductUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class AddEditProductUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val editingProductId: Long? = null,
    val name: String = "",
    val barcode: String = "",
    val categoryId: Long? = null,
    val basePrice: String = "",
    val costPrice: String = "",
    val sellingPrice: String = "",
    val stockQuantity: String = "",
    val reorderLevel: String = "",
    val unit: String = "pcs",
    val description: String = "",
    val categories: List<Category> = emptyList(),
    val nameError: String? = null,
    val sellingPriceError: String? = null,
    val error: String? = null,
    val enableTax: Boolean = false,
    val taxRate: Double = 0.12
)

sealed class AddEditProductEvent {
    data object SaveSuccess : AddEditProductEvent()
    data class SaveError(val message: String) : AddEditProductEvent()
}

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditProductUiState())
    val uiState: StateFlow<AddEditProductUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditProductEvent>()
    val events: SharedFlow<AddEditProductEvent> = _events.asSharedFlow()

    private var loadedProductCreatedAt: Long = System.currentTimeMillis()

    fun loadPreferences() {
        viewModelScope.launch {
            val enableTax = appPreferences.enableTax.first()
            val taxRate = appPreferences.taxRate.first()
            _uiState.value = _uiState.value.copy(
                enableTax = enableTax,
                taxRate = if (taxRate > 0.0) taxRate else 0.12
            )
        }
    }

    fun loadProduct(productId: Long?) {
        if (productId == null || productId == 0L) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val product = productRepository.getProductByIdOnce(productId)
            if (product != null) {
                loadedProductCreatedAt = product.createdAt
                val basePriceStr = if (product.basePrice > 0) product.basePrice.toString() else ""
                val sellingPriceStr = if (product.sellingPrice > 0) product.sellingPrice.toString() else ""
                _uiState.value = AddEditProductUiState(
                    isLoading = false,
                    isEditMode = true,
                    editingProductId = product.id,
                    name = product.name,
                    barcode = product.barcode ?: "",
                    categoryId = product.categoryId,
                    basePrice = basePriceStr,
                    costPrice = if (product.costPrice > 0) product.costPrice.toString() else "",
                    sellingPrice = sellingPriceStr,
                    stockQuantity = product.stockQuantity.toString(),
                    reorderLevel = product.reorderLevel.toString(),
                    unit = product.unit,
                    description = product.description ?: "",
                    categories = _uiState.value.categories,
                    enableTax = _uiState.value.enableTax,
                    taxRate = _uiState.value.taxRate
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Product not found")
            }
        }
        loadCategories()
    }

    fun setBarcode(barcode: String) {
        _uiState.value = _uiState.value.copy(barcode = barcode)
    }

    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .catch { }
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
        }
    }

    fun createCategory(name: String): Boolean {
        var success = false
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val category = Category(name = name.trim(), createdAt = now, updatedAt = now)
                val id = categoryRepository.insert(category)
                _uiState.value = _uiState.value.copy(categoryId = id)
                success = true
            } catch (_: Exception) { }
        }
        return success
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onBarcodeChange(value: String) {
        _uiState.value = _uiState.value.copy(barcode = value)
    }

    fun onCategorySelected(id: Long?) {
        _uiState.value = _uiState.value.copy(categoryId = id)
    }

    fun onBasePriceChange(value: String) {
        if (value.isNotEmpty() && !value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) return
        _uiState.value = _uiState.value.copy(basePrice = value, sellingPriceError = null)
        autoCalcSellingPrice(value)
    }

    private fun autoCalcSellingPrice(basePriceStr: String) {
        val state = _uiState.value
        val base = basePriceStr.toDoubleOrNull()
        if (base != null && base > 0 && state.enableTax) {
            val inclusive = base * (1.0 + state.taxRate)
            val formatted = String.format(Locale.US, "%.2f", inclusive)
            _uiState.value = _uiState.value.copy(sellingPrice = formatted)
        } else if (base != null && base > 0) {
            _uiState.value = _uiState.value.copy(sellingPrice = basePriceStr)
        }
    }

    fun onCostPriceChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.value = _uiState.value.copy(costPrice = value)
        }
    }

    fun onSellingPriceChange(value: String) {
        if (_uiState.value.enableTax && _uiState.value.basePrice.toDoubleOrNull() != null && _uiState.value.basePrice.toDoubleOrNull()!! > 0) return
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.value = _uiState.value.copy(sellingPrice = value, sellingPriceError = null)
        }
    }

    fun onStockQuantityChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(stockQuantity = value)
        }
    }

    fun onReorderLevelChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(reorderLevel = value)
        }
    }

    fun onUnitChange(value: String) {
        _uiState.value = _uiState.value.copy(unit = value)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun resetForm() {
        val currentCategories = _uiState.value.categories
        _uiState.value = AddEditProductUiState(categories = currentCategories)
        loadPreferences()
    }

    fun save() {
        val state = _uiState.value
        var hasError = false

        if (state.name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = "Product name is required")
            hasError = true
        }

        val sellingPrice = state.sellingPrice.toDoubleOrNull()
        if (sellingPrice == null || sellingPrice <= 0) {
            _uiState.value = _uiState.value.copy(sellingPriceError = "Valid selling price is required")
            hasError = true
        }

        if (hasError) return

        _uiState.value = _uiState.value.copy(isSaving = true, error = null)

        viewModelScope.launch {
            val product = Product(
                id = state.editingProductId ?: 0L,
                name = state.name.trim(),
                barcode = state.barcode.trim().ifBlank { null },
                categoryId = state.categoryId,
                description = state.description.trim().ifBlank { null },
                costPrice = state.costPrice.toDoubleOrNull() ?: 0.0,
                basePrice = state.basePrice.toDoubleOrNull() ?: 0.0,
                sellingPrice = sellingPrice ?: 0.0,
                stockQuantity = state.stockQuantity.toIntOrNull() ?: 0,
                reorderLevel = state.reorderLevel.toIntOrNull() ?: 0,
                unit = state.unit,
                createdAt = if (state.isEditMode) loadedProductCreatedAt else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (state.isEditMode) {
                updateProductUseCase(product).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isSaving = false)
                        _events.emit(AddEditProductEvent.SaveSuccess)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(isSaving = false)
                        _events.emit(AddEditProductEvent.SaveError(e.message ?: "Failed to update product"))
                    }
                )
            } else {
                addProductUseCase(product).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isSaving = false)
                        _events.emit(AddEditProductEvent.SaveSuccess)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(isSaving = false)
                        _events.emit(AddEditProductEvent.SaveError(e.message ?: "Failed to add product"))
                    }
                )
            }
        }
    }
}
