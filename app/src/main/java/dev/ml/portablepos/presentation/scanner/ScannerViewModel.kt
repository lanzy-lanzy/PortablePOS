package dev.ml.portablepos.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.ScannerMode
import dev.ml.portablepos.domain.usecase.BarcodeScanResult
import dev.ml.portablepos.domain.usecase.ProcessBarcodeScanUseCase
import dev.ml.portablepos.presentation.pos.CartManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScannerNavigation {
    data class ProductNotFound(val barcode: String) : ScannerNavigation()
    data class BarcodeExists(val barcode: String) : ScannerNavigation()
    data class NavigateToAddProduct(val barcode: String) : ScannerNavigation()
    object GoBack : ScannerNavigation()
}

data class ScannerUiState(
    val isProcessing: Boolean = false,
    val showProductNotFoundDialog: Boolean = false,
    val notFoundBarcode: String? = null,
    val showBarcodeExistsError: Boolean = false,
    val message: String? = null,
    val torchOn: Boolean = false,
    val lastAddedProductName: String? = null,
    val totalCartItems: Int = 0
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val processBarcodeScanUseCase: ProcessBarcodeScanUseCase,
    private val cartManager: CartManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _navigation = MutableSharedFlow<ScannerNavigation>()
    val navigation: SharedFlow<ScannerNavigation> = _navigation.asSharedFlow()

    var scanMode: ScannerMode = ScannerMode.SALE

    private var lastScannedBarcode: String? = null
    private var lastScanTime: Long = 0L

    fun isDuplicateScan(barcode: String): Boolean {
        val now = System.currentTimeMillis()
        if (barcode == lastScannedBarcode && now - lastScanTime < 1000L) return true
        lastScannedBarcode = barcode
        lastScanTime = now
        return false
    }

    fun setMode(mode: String) {
        scanMode = try {
            ScannerMode.valueOf(mode)
        } catch (e: Exception) {
            ScannerMode.SALE
        }
    }

    fun processBarcode(barcode: String) {
        if (_uiState.value.isProcessing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            when (val result = processBarcodeScanUseCase(barcode)) {
                is BarcodeScanResult.ProductFound -> {
                    when (scanMode) {
                        ScannerMode.PRODUCT_REGISTRATION -> {
                            _uiState.update { it.copy(isProcessing = false, showBarcodeExistsError = true, message = "Barcode already exists: ${result.product.name}") }
                            _navigation.emit(ScannerNavigation.BarcodeExists(barcode))
                        }
                        ScannerMode.SALE -> {
                            cartManager.addItem(result.product)
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    lastAddedProductName = result.product.name,
                                    totalCartItems = cartManager.itemCount
                                )
                            }
                        }
                    }
                }
                is BarcodeScanResult.ProductNotFound -> {
                    when (scanMode) {
                        ScannerMode.PRODUCT_REGISTRATION -> {
                            _uiState.update { it.copy(isProcessing = false) }
                            _navigation.emit(ScannerNavigation.NavigateToAddProduct(barcode))
                        }
                        ScannerMode.SALE -> {
                            _uiState.update { it.copy(isProcessing = false, showProductNotFoundDialog = true, notFoundBarcode = barcode) }
                        }
                    }
                }
                is BarcodeScanResult.Error -> {
                    _uiState.update { it.copy(isProcessing = false, message = result.message) }
                }
            }
        }
    }

    fun dismissProductNotFoundDialog() {
        _uiState.update { it.copy(showProductNotFoundDialog = false, notFoundBarcode = null) }
    }

    fun navigateToAddProduct() {
        val barcode = _uiState.value.notFoundBarcode ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showProductNotFoundDialog = false) }
            _navigation.emit(ScannerNavigation.NavigateToAddProduct(barcode))
        }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(torchOn = !it.torchOn) }
    }

    fun clearLastAddedProduct() {
        _uiState.update { it.copy(lastAddedProductName = null) }
    }

    fun doneScanning() {
        viewModelScope.launch {
            _navigation.emit(ScannerNavigation.GoBack)
        }
    }

    fun goBack() {
        viewModelScope.launch {
            _navigation.emit(ScannerNavigation.GoBack)
        }
    }
}
