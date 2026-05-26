package dev.ml.portablepos.presentation.pos

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.data.local.preferences.AppPreferences
import dev.ml.portablepos.domain.usecase.CompleteSaleUseCase
import dev.ml.portablepos.util.QrCodeUtil
import dev.ml.portablepos.util.formatAmount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CheckoutUiState(
    val cashReceived: String = "",
    val changeAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val saleId: Long? = null,
    val error: String? = null,
    val showConfirmDialog: Boolean = false,
    val paymentMethod: String = "Cash",
    val enableTax: Boolean = false,
    val taxRate: Double = 0.0,
    val cashierName: String = "Cashier",
    val gcashReference: String = "",
    val gcashQrBitmap: Bitmap? = null,
    val gcashNumber: String = ""
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val completeSaleUseCase: CompleteSaleUseCase,
    val cartManager: CartManager,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    val subtotal: Double get() = cartManager.subtotal
    val discount: Double get() = cartManager.discount.value

    val taxAmount: Double get() {
        val s = _uiState.value
        return if (s.enableTax && s.taxRate > 0) {
            val taxable = subtotal - discount
            taxable * s.taxRate / (1.0 + s.taxRate)
        } else 0.0
    }

    val grandTotal: Double get() = subtotal - discount

    init {
        viewModelScope.launch {
            appPreferences.enableTax.first().let { enabled ->
                _uiState.update { it.copy(enableTax = enabled) }
            }
        }
        viewModelScope.launch {
            appPreferences.taxRate.first().let { rate ->
                _uiState.update { it.copy(taxRate = rate) }
            }
        }
        viewModelScope.launch {
            appPreferences.cashierName.first().let { name ->
                _uiState.update { it.copy(cashierName = name) }
            }
        }
        viewModelScope.launch {
            appPreferences.gcashNumber.first().let { number ->
                _uiState.update { it.copy(gcashNumber = number) }
            }
        }
    }

    val items get() = cartManager.items.value

    fun setPaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method, gcashReference = "", error = null) }
        if (method == "GCash") {
            generateGcashQrCode()
        }
    }

    private fun generateGcashQrCode() {
        val storeName = "PortablePOS"
        val amount = grandTotal
        val gcashNum = _uiState.value.gcashNumber
        val qrContent = "GCASH:$gcashNum:${formatAmount(amount)}:$storeName"
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                QrCodeUtil.generateQrCode(qrContent)
            }
            _uiState.update { it.copy(gcashQrBitmap = bitmap) }
        }
    }

    fun updateGcashReference(value: String) {
        if (value.length <= 20) {
            _uiState.update { it.copy(gcashReference = value, error = null) }
        }
    }

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

    fun showConfirmDialog() { _uiState.update { it.copy(showConfirmDialog = true) } }
    fun dismissConfirmDialog() { _uiState.update { it.copy(showConfirmDialog = false) } }

    fun validateAndShowConfirm(): Boolean {
        val state = _uiState.value
        if (state.paymentMethod == "GCash" && state.gcashReference.isBlank()) {
            _uiState.update { it.copy(error = "Please enter the GCash reference number") }
            return false
        }
        if (state.paymentMethod == "Cash") {
            val cashReceived = state.cashReceived.toDoubleOrNull() ?: 0.0
            if (cashReceived < grandTotal) {
                _uiState.update { it.copy(error = "Cash received must be at least ${formatAmount(grandTotal)}") }
                return false
            }
        }
        _uiState.update { it.copy(showConfirmDialog = true) }
        return true
    }

    fun completeSale() {
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, showConfirmDialog = false) }
            val result = completeSaleUseCase(
                cartItems = items,
                cashReceived = if (state.paymentMethod == "Cash") state.cashReceived.toDoubleOrNull() ?: 0.0 else grandTotal,
                discount = discount,
                cashierName = state.cashierName,
                paymentMethod = state.paymentMethod,
                paymentReference = if (state.paymentMethod == "GCash") state.gcashReference else ""
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

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
