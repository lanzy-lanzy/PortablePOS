package dev.ml.portablepos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.ml.portablepos.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleItemDao {

    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId")
    fun getItemsBySaleId(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items GROUP BY product_id ORDER BY SUM(quantity) DESC LIMIT :limit")
    fun getBestSellingProducts(limit: Int): Flow<List<SaleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSaleItem(saleItem: SaleItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Update
    suspend fun updateSaleItem(saleItem: SaleItemEntity)

    @Delete
    suspend fun deleteSaleItem(saleItem: SaleItemEntity)
}
