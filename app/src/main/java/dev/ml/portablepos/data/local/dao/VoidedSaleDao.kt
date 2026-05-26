package dev.ml.portablepos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.ml.portablepos.data.local.entity.VoidedSaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoidedSaleDao {

    @Query("SELECT * FROM voided_sales ORDER BY voided_at DESC")
    fun getAllVoidedSales(): Flow<List<VoidedSaleEntity>>

    @Query("SELECT * FROM voided_sales WHERE sale_id = :saleId LIMIT 1")
    suspend fun getVoidedSaleBySaleId(saleId: Long): VoidedSaleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVoidedSale(voidedSale: VoidedSaleEntity): Long
}