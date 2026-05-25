package dev.ml.portablepos.presentation.saleshistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.SaleItem
import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SaleDetailUiState(
    val sale: Sale? = null,
    val saleItems: List<SaleItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SaleDetailViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SaleDetailUiState())
    val uiState: StateFlow<SaleDetailUiState> = _uiState.asStateFlow()

    fun loadSale(saleId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val sale = saleRepository.getSaleById(saleId)
                _uiState.update { it.copy(sale = sale, isLoading = false) }
                if (sale != null) {
                    saleRepository.getSaleItems(saleId)
                        .catch { e ->
                            _uiState.update { it.copy(error = e.message ?: "Failed to load items") }
                        }
                        .collect { items ->
                            _uiState.update { it.copy(saleItems = items, isLoading = false) }
                        }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load sale details") }
            }
        }
    }
}
