package dev.ml.portablepos.presentation.receipt

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ml.portablepos.data.local.preferences.AppPreferences
import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.SaleItem
import dev.ml.portablepos.domain.repository.SaleRepository
import dev.ml.portablepos.util.ReceiptPrinter
import dev.ml.portablepos.util.formatAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ReceiptUiState(
    val sale: Sale? = null,
    val saleItems: List<SaleItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val storeName: String = "PortablePOS",
    val storeAddress: String = "",
    val storeContact: String = "",
    val receiptFooter: String = "",
    val printerEnabled: Boolean = false,
    val printerAddress: String = "",
    val enableTax: Boolean = false,
    val taxRate: Double = 0.0,
    val isPrinting: Boolean = false
)

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saleRepository: SaleRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    fun loadSale(saleId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val storeNameValue = appPreferences.storeName.first()
                _uiState.update { it.copy(storeName = storeNameValue.ifBlank { "PortablePOS" }) }
                val sale = saleRepository.getSaleById(saleId)
                _uiState.update { it.copy(sale = sale) }
                if (sale != null) {
                    saleRepository.getSaleItems(saleId)
                        .catch { e -> _uiState.update { it.copy(error = e.message ?: "Failed to load items") } }
                        .collect { items -> _uiState.update { it.copy(saleItems = items, isLoading = false) } }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Sale not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load receipt") }
            }
        }
        viewModelScope.launch {
            appPreferences.storeName.collect { _uiState.update { it.copy(storeName = it.storeName.ifBlank { "PortablePOS" }) } }
        }
        viewModelScope.launch { appPreferences.storeAddress.collect { _uiState.update { it.copy(storeAddress = it.storeAddress) } } }
        viewModelScope.launch { appPreferences.storeContact.collect { _uiState.update { it.copy(storeContact = it.storeContact) } } }
        viewModelScope.launch { appPreferences.receiptFooter.collect { _uiState.update { it.copy(receiptFooter = it.receiptFooter) } } }
        viewModelScope.launch { appPreferences.printerEnabled.collect { _uiState.update { it.copy(printerEnabled = it.printerEnabled) } } }
        viewModelScope.launch { appPreferences.printerAddress.collect { _uiState.update { it.copy(printerAddress = it.printerAddress) } } }
        viewModelScope.launch { appPreferences.enableTax.collect { _uiState.update { it.copy(enableTax = it.enableTax) } } }
        viewModelScope.launch { appPreferences.taxRate.collect { _uiState.update { it.copy(taxRate = it.taxRate) } } }
    }

    fun printReceipt() {
        val state = _uiState.value
        val sale = state.sale ?: return
        if (!state.printerEnabled || state.printerAddress.isBlank()) {
            _uiState.update { it.copy(error = "Printer not configured. Set up in Settings.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPrinting = true) }
            val receiptData = ReceiptPrinter.ReceiptData(
                storeName = state.storeName,
                storeAddress = state.storeAddress,
                storeContact = state.storeContact,
                footer = state.receiptFooter,
                showTax = state.enableTax,
                taxRate = state.taxRate
            )
            val receiptText = ReceiptPrinter.buildReceiptText(sale, state.saleItems, receiptData)
            val result = ReceiptPrinter.printToBluetooth(state.printerAddress, receiptText)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isPrinting = false) } },
                onFailure = { e -> _uiState.update { it.copy(isPrinting = false, error = e.message ?: "Print failed") } }
            )
        }
    }

    fun shareReceipt() {
        val sale = _uiState.value.sale ?: return
        val text = buildReceiptText(sale, _uiState.value.saleItems)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Receipt"))
    }

    private fun buildReceiptText(sale: Sale, items: List<SaleItem>): String {
        val sb = StringBuilder()
        sb.appendLine(_uiState.value.storeName.ifBlank { "PortablePOS" })
        sb.appendLine("================================")
        sb.appendLine("TXN: ${sale.transactionNumber}")
        sb.appendLine("Cashier: ${sale.cashierName}")
        sb.appendLine("Payment: ${sale.paymentMethod}")
        sb.appendLine("================================")
        for (item in items) {
            sb.appendLine("${item.productName} x${item.quantity} @ ${formatAmount(item.unitPrice)}")
            sb.appendLine("  ${formatAmount(item.totalPrice)}")
        }
        sb.appendLine("--------------------------------")
        if (_uiState.value.enableTax && _uiState.value.taxRate > 0) {
            sb.appendLine("Subtotal: ${formatAmount(sale.subtotal)}")
            val vat = sale.subtotal * _uiState.value.taxRate / (1.0 + _uiState.value.taxRate)
            sb.appendLine("VAT: ${formatAmount(vat)}")
        }
        sb.appendLine("Discount: ${formatAmount(sale.discount)}")
        sb.appendLine("TOTAL: ${formatAmount(sale.totalAmount)}")
        sb.appendLine("Cash: ${formatAmount(sale.cashReceived)}")
        sb.appendLine("Change: ${formatAmount(sale.changeAmount)}")
        sb.appendLine("================================")
        sb.appendLine("Thank you!")
        return sb.toString()
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}