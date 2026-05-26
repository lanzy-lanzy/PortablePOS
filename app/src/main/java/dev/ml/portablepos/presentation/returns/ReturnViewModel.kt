package dev.ml.portablepos.presentation.returns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.ItemReturnInput
import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.SaleItem
import dev.ml.portablepos.domain.repository.SaleRepository
import dev.ml.portablepos.domain.usecase.ProcessReturnUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReturnUiState(
    val sale: Sale? = null,
    val saleItems: List<SaleItem> = emptyList(),
    val selectedReturns: Map<Long, Int> = emptyMap(),
    val reason: String = "",
    val isProcessing: Boolean = false,
    val isSuccess: Boolean = false,
    val returnRecordId: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class ReturnItemInput(
    val saleItem: SaleItem,
    val returnQuantity: Int = 0,
    val maxReturnable: Int = 0
)

@HiltViewModel
class ReturnViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val processReturnUseCase: ProcessReturnUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReturnUiState())
    val uiState: StateFlow<ReturnUiState> = _uiState.asStateFlow()

    fun loadSale(saleId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val sale = saleRepository.getSaleById(saleId)
                _uiState.update { it.copy(sale = sale) }
                if (sale != null) {
                    saleRepository.getReturnableItems(saleId)
                        .catch { e ->
                            _uiState.update { it.copy(error = e.message ?: "Failed to load items") }
                        }
                        .collect { items ->
                            _uiState.update { it.copy(saleItems = items, isLoading = false) }
                        }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Sale not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load sale") }
            }
        }
    }

    fun updateReturnQuantity(productId: Long, quantity: Int) {
        _uiState.update { state ->
            val updated = state.selectedReturns.toMutableMap()
            if (quantity <= 0) {
                updated.remove(productId)
            } else {
                updated[productId] = quantity
            }
            state.copy(selectedReturns = updated)
        }
    }

    fun updateReason(reason: String) {
        _uiState.update { it.copy(reason = reason) }
    }

    fun processReturn() {
        val state = _uiState.value
        val sale = state.sale ?: return
        val selected = state.selectedReturns

        if (selected.isEmpty()) {
            _uiState.update { it.copy(error = "Select at least one item to return") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                val items = selected.map { (productId, quantity) ->
                    val saleItem = state.saleItems.find { it.productId == productId }!!
                    ItemReturnInput(
                        productId = productId,
                        quantity = quantity,
                        refundAmount = quantity * saleItem.unitPrice
                    )
                }

                val result = processReturnUseCase(
                    saleId = sale.id,
                    items = items,
                    reason = state.reason
                )

                result.fold(
                    onSuccess = { recordId ->
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                isSuccess = true,
                                returnRecordId = recordId
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isProcessing = false, error = e.message ?: "Return failed")
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, error = e.message ?: "Return failed")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
