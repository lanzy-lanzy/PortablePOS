package dev.ml.portablepos.domain.repository

import dev.ml.portablepos.domain.model.CloseoutRecord
import kotlinx.coroutines.flow.Flow

interface CloseoutRepository {
    fun getAllCloseouts(): Flow<List<CloseoutRecord>>
    suspend fun getCloseoutById(id: Long): CloseoutRecord?
    suspend fun getLatestCloseoutByCashier(cashierId: Long): CloseoutRecord?
    suspend fun getTodayCloseoutByCashier(cashierName: String, startOfDay: Long, endOfDay: Long): CloseoutRecord?
    suspend fun createCloseout(closeout: CloseoutRecord): Long
}