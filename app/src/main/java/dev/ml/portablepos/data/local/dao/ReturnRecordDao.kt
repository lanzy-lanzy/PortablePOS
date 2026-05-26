package dev.ml.portablepos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.ml.portablepos.data.local.entity.ReturnRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnRecordDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: ReturnRecordEntity): Long

    @Query("SELECT * FROM return_records WHERE sale_id = :saleId ORDER BY created_at DESC")
    fun getBySaleId(saleId: Long): Flow<List<ReturnRecordEntity>>

    @Query("SELECT * FROM return_records ORDER BY created_at DESC")
    fun getAll(): Flow<List<ReturnRecordEntity>>
}
