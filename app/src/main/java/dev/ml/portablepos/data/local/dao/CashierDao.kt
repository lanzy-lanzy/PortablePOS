package dev.ml.portablepos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.ml.portablepos.data.local.entity.CashierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashierDao {

    @Query("SELECT * FROM cashiers ORDER BY full_name ASC")
    fun getAllCashiers(): Flow<List<CashierEntity>>

    @Query("SELECT * FROM cashiers WHERE id = :id")
    fun getCashierById(id: Long): Flow<CashierEntity?>

    @Query("SELECT * FROM cashiers WHERE id = :id")
    suspend fun getCashierByIdOnce(id: Long): CashierEntity?

    @Query("SELECT * FROM cashiers WHERE username = :username LIMIT 1")
    suspend fun getCashierByUsername(username: String): CashierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashier(cashier: CashierEntity): Long

    @Update
    suspend fun updateCashier(cashier: CashierEntity)

    @Delete
    suspend fun deleteCashier(cashier: CashierEntity)
}
