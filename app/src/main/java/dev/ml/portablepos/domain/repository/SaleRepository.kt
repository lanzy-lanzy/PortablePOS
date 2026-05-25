package dev.ml.portablepos.domain.repository

import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.SaleItem
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun getAllSales(): Flow<List<Sale>>
    suspend fun getSaleById(id: Long): Sale?
    suspend fun getSaleByTransactionNumber(txnNumber: String): Sale?
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>>
    fun searchByTransactionNumber(query: String): Flow<List<Sale>>
    fun getTotalSalesToday(todayStart: Long, todayEnd: Long): Flow<Double>
    fun getTransactionCountToday(todayStart: Long, todayEnd: Long): Flow<Int>
    fun getTodaySales(todayStart: Long, todayEnd: Long): Flow<List<Sale>>
    fun getTotalSalesThisWeek(weekStart: Long, weekEnd: Long): Flow<Double>
    fun getTotalSalesThisMonth(monthStart: Long, monthEnd: Long): Flow<Double>
    fun getTotalDiscounts(todayStart: Long, todayEnd: Long): Flow<Double>
    fun getTotalGrossSales(todayStart: Long, todayEnd: Long): Flow<Double>
    suspend fun insertSale(sale: Sale): Long
    suspend fun updateSale(sale: Sale)
    suspend fun deleteSale(sale: Sale)
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>>
    suspend fun completeSale(sale: Sale, saleItems: List<SaleItem>, productStocks: Map<Long, Int>): Long
}
