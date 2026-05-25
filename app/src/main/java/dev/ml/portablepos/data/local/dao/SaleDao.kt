package dev.ml.portablepos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.ml.portablepos.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY created_at DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE transaction_number = :txnNumber")
    suspend fun getSaleByTransactionNumber(txnNumber: String): SaleEntity?

    @Query("SELECT * FROM sales WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE transaction_number LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchByTransactionNumber(query: String): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE created_at BETWEEN :todayStart AND :todayEnd")
    fun getTotalSalesToday(todayStart: Long, todayEnd: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM sales WHERE created_at BETWEEN :todayStart AND :todayEnd")
    fun getTransactionCountToday(todayStart: Long, todayEnd: Long): Flow<Int>

    @Query("SELECT * FROM sales WHERE created_at BETWEEN :todayStart AND :todayEnd ORDER BY created_at DESC")
    fun getTodaySales(todayStart: Long, todayEnd: Long): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE created_at BETWEEN :weekStart AND :weekEnd")
    fun getTotalSalesThisWeek(weekStart: Long, weekEnd: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE created_at BETWEEN :monthStart AND :monthEnd")
    fun getTotalSalesThisMonth(monthStart: Long, monthEnd: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(discount), 0) FROM sales WHERE created_at BETWEEN :todayStart AND :todayEnd")
    fun getTotalDiscounts(todayStart: Long, todayEnd: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(subtotal), 0) FROM sales WHERE created_at BETWEEN :todayStart AND :todayEnd")
    fun getTotalGrossSales(todayStart: Long, todayEnd: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSale(sale: SaleEntity): Long

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)
}
