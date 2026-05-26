package dev.ml.portablepos.presentation.returns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.ReturnRecord
import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReturnHistoryUiState(
    val returns: List<ReturnRecord> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ReturnHistoryViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReturnHistoryUiState())
    val uiState: StateFlow<ReturnHistoryUiState> = _uiState.asStateFlow()

    init {
        loadReturns()
    }

    fun loadReturns() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            saleRepository.getAllReturnRecords()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load returns") }
                }
                .collect { records ->
                    _uiState.update { it.copy(returns = records, isLoading = false) }
                }
        }
    }
}
