package dev.ml.portablepos.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.domain.model.Cashier
import dev.ml.portablepos.domain.repository.CashierRepository
import dev.ml.portablepos.presentation.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val pinCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val loggedInCashier: Cashier? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val cashierRepository: CashierRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ensureAdminExists()
        }
    }

    private suspend fun ensureAdminExists() {
        val cashiers = cashierRepository.getAllCashiers().first()
        if (cashiers.isEmpty()) {
            cashierRepository.addCashier(
                Cashier(
                    fullName = "Admin",
                    username = "admin",
                    role = "Admin",
                    pinCode = "1234",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun updatePinCode(value: String) {
        if (value.length <= 6) {
            _uiState.update { it.copy(pinCode = value, error = null) }
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank()) {
            _uiState.update { it.copy(error = "Username is required") }
            return
        }
        if (state.pinCode.length < 4) {
            _uiState.update { it.copy(error = "PIN must be at least 4 digits") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val cashier = cashierRepository.getCashierByUsername(state.username)
                if (cashier == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Invalid username") }
                    return@launch
                }
                if (cashier.pinCode != state.pinCode) {
                    _uiState.update { it.copy(isLoading = false, error = "Invalid PIN") }
                    return@launch
                }
                sessionManager.startSession(cashier)
                _uiState.update {
                    it.copy(isLoading = false, isLoggedIn = true, loggedInCashier = cashier)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}