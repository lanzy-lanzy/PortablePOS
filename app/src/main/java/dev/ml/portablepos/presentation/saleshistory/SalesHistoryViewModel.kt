package dev.ml.portablepos.presentation.saleshistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SalesHistoryUiState(
    val sales: List<Sale> = emptyList(),
    val searchQuery: String = "",
    val filteredSales: List<Sale> = emptyList(),
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesHistoryUiState())
    val uiState: StateFlow<SalesHistoryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSales()
    }

    fun loadSales() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            saleRepository.getAllSales()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load sales") }
                }
                .collect { sales ->
                    _uiState.update {
                        it.copy(
                            sales = sales,
                            filteredSales = filterSales(sales, it.searchQuery),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            val current = _uiState.value
            if (query.isBlank()) {
                _uiState.update { it.copy(filteredSales = current.sales) }
            } else {
                saleRepository.searchByTransactionNumber(query)
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message ?: "Search failed") }
                    }
                    .collect { filtered ->
                        _uiState.update { it.copy(filteredSales = filtered) }
                    }
            }
        }
    }

    fun onDateRangeSelected(start: Long, end: Long) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        viewModelScope.launch {
            saleRepository.getSalesByDateRange(start, end)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to filter by date") }
                }
                .collect { filtered ->
                    _uiState.update { it.copy(filteredSales = filtered) }
                }
        }
    }

    fun clearDateFilter() {
        _uiState.update { it.copy(startDate = null, endDate = null, filteredSales = _uiState.value.sales) }
    }

    private fun filterSales(sales: List<Sale>, query: String): List<Sale> {
        if (query.isBlank()) return sales
        return sales.filter { sale ->
            sale.transactionNumber.contains(query, ignoreCase = true) ||
                sale.cashierName.contains(query, ignoreCase = true)
        }
    }
}
