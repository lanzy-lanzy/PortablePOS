package dev.ml.portablepos.domain.repository

import dev.ml.portablepos.domain.model.Cashier
import kotlinx.coroutines.flow.Flow

interface CashierRepository {
    fun getAllCashiers(): Flow<List<Cashier>>
    fun getCashierById(id: Long): Flow<Cashier?>
    suspend fun getCashierByIdOnce(id: Long): Cashier?
    suspend fun getCashierByUsername(username: String): Cashier?
    suspend fun addCashier(cashier: Cashier): Long
    suspend fun updateCashier(cashier: Cashier)
    suspend fun deleteCashier(cashier: Cashier)
}
