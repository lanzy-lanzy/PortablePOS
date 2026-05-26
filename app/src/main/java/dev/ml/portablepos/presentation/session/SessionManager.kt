package dev.ml.portablepos.presentation.session

import dev.ml.portablepos.domain.model.Cashier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    companion object {
        const val INACTIVITY_TIMEOUT_MS = 300_000L
    }

    private val _currentCashier = MutableStateFlow<Cashier?>(null)
    val currentCashier: StateFlow<Cashier?> = _currentCashier.asStateFlow()

    val isLoggedIn: Boolean get() = _currentCashier.value != null

    private val lastActivityTime = AtomicLong(0L)

    fun startSession(cashier: Cashier) {
        _currentCashier.value = cashier
        lastActivityTime.set(System.currentTimeMillis())
    }

    fun recordActivity() {
        if (isLoggedIn) {
            lastActivityTime.set(System.currentTimeMillis())
        }
    }

    fun endSession() {
        _currentCashier.value = null
        lastActivityTime.set(0L)
    }

    fun isSessionExpired(): Boolean {
        if (_currentCashier.value == null) return false
        return System.currentTimeMillis() - lastActivityTime.get() > INACTIVITY_TIMEOUT_MS
    }
}
