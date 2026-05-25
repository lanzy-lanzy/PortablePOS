package dev.ml.portablepos.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.usecase.AdjustStockUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AdjustmentType { STOCK_IN, STOCK_OUT, ADJUSTMENT }

data class StockAdjustmentUiState(
    val product: Product? = null,
    val adjustmentType: AdjustmentType = AdjustmentType.STOCK_IN,
    val quantity: String = "",
    val reason: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val currentStock: Int get() = product?.stockQuantity ?: 0
    val quantityValue: Int get() = quantity.toIntOrNull() ?: 0
    val newStockPreview: Int get() {
        return when (adjustmentType) {
            AdjustmentType.STOCK_IN -> currentStock + quantityValue
            AdjustmentType.STOCK_OUT -> currentStock - quantityValue
            AdjustmentType.ADJUSTMENT -> quantityValue
        }
    }
    val isValid: Boolean get() {
        if (quantityValue <= 0) return false
        if (newStockPreview < 0) return false
        return true
    }
}

@HiltViewModel
class StockAdjustmentViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val adjustStockUseCase: AdjustStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockAdjustmentUiState())
    val uiState: StateFlow<StockAdjustmentUiState> = _uiState.asStateFlow()

    fun loadProduct(productId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val product = productRepository.getProductByIdOnce(productId)
                _uiState.update { it.copy(product = product, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load product") }
            }
        }
    }

    fun setAdjustmentType(type: AdjustmentType) {
        _uiState.update { it.copy(adjustmentType = type) }
    }

    fun updateQuantity(value: String) {
        val cleanValue = value.filter { it.isDigit() }
        _uiState.update { it.copy(quantity = cleanValue) }
    }

    fun updateReason(reason: String) {
        _uiState.update { it.copy(reason = reason) }
    }

    fun saveAdjustment() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = adjustStockUseCase(
                productId = state.product!!.id,
                newStock = state.newStockPreview,
                reason = state.reason.ifBlank { null }
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save adjustment") }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
