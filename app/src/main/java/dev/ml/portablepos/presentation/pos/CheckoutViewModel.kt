package dev.ml.portablepos.presentation.pos
import dev.ml.portablepos.util.formatAmount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.usecase.CompleteSaleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val cashReceived: String = "",
    val changeAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val saleId: Long? = null,
    val error: String? = null,
    val showConfirmDialog: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val completeSaleUseCase: CompleteSaleUseCase,
    val cartManager: CartManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    val subtotal: Double get() = cartManager.subtotal
    val discount: Double get() = cartManager.discount.value
    val grandTotal: Double get() = subtotal - discount
    val items get() = cartManager.items.value

    fun updateCashReceived(value: String) {
        val cleanValue = value.filter { it.isDigit() || it == '.' }
        if (cleanValue.count { it == '.' } <= 1) {
            _uiState.update { it.copy(cashReceived = cleanValue) }
            updateChange(cleanValue)
        }
    }

    private fun updateChange(cashReceivedStr: String) {
        val cash = cashReceivedStr.toDoubleOrNull() ?: 0.0
        val change = if (cash >= grandTotal) cash - grandTotal else 0.0
        _uiState.update { it.copy(changeAmount = change) }
    }

    fun showConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun dismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun completeSale() {
        val cashReceived = _uiState.value.cashReceived.toDoubleOrNull() ?: 0.0
        if (cashReceived < grandTotal) {
            _uiState.update { it.copy(error = "Cash received must be at least ${formatAmount(grandTotal)}") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, showConfirmDialog = false) }
            val result = completeSaleUseCase(
                cartItems = items,
                cashReceived = cashReceived,
                discount = discount
            )
            result.fold(
                onSuccess = { saleId ->
                    cartManager.clear()
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, saleId = saleId) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Sale failed") }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
