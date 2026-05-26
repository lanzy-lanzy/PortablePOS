package dev.ml.portablepos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.ml.portablepos.data.local.entity.CloseoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CloseoutDao {

    @Query("SELECT * FROM closeout_records ORDER BY closed_at DESC")
    fun getAllCloseouts(): Flow<List<CloseoutEntity>>

    @Query("SELECT * FROM closeout_records WHERE id = :id")
    suspend fun getCloseoutById(id: Long): CloseoutEntity?

    @Query("SELECT * FROM closeout_records WHERE cashier_id = :cashierId ORDER BY closed_at DESC LIMIT 1")
    suspend fun getLatestCloseoutByCashier(cashierId: Long): CloseoutEntity?

    @Query("SELECT * FROM closeout_records WHERE cashier_name = :cashierName AND closed_at BETWEEN :startOfDay AND :endOfDay ORDER BY closed_at DESC LIMIT 1")
    suspend fun getTodayCloseoutByCashier(cashierName: String, startOfDay: Long, endOfDay: Long): CloseoutEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCloseout(closeout: CloseoutEntity): Long
}