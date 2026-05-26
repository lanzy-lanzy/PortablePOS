package dev.ml.portablepos.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.data.local.preferences.AppPreferences
import dev.ml.portablepos.domain.usecase.DashboardData
import dev.ml.portablepos.domain.usecase.GetDashboardDataUseCase
import dev.ml.portablepos.presentation.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val data: DashboardData = DashboardData(),
    val error: String? = null,
    val storeName: String = "PortablePOS",
    val cashierName: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val appPreferences: AppPreferences,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        loadDashboard()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val name = appPreferences.storeName.first()
            _uiState.update { it.copy(storeName = name.ifBlank { "PortablePOS" }) }
        }
        viewModelScope.launch {
            sessionManager.currentCashier.collect { cashier ->
                _uiState.update { it.copy(cashierName = cashier?.fullName ?: "Cashier") }
            }
        }
        viewModelScope.launch {
            appPreferences.storeName.collect { name ->
                _uiState.update { it.copy(storeName = name.ifBlank { "PortablePOS" }) }
            }
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            getDashboardDataUseCase()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true, error = null) }
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
                .collect { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        data = data,
                        error = null
                    )
                }
        }
    }
}
