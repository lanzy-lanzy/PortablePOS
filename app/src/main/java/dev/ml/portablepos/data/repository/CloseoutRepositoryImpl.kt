package dev.ml.portablepos.data.repository

import dev.ml.portablepos.data.local.dao.CloseoutDao
import dev.ml.portablepos.data.mapper.toDomainModel
import dev.ml.portablepos.data.mapper.toEntity
import dev.ml.portablepos.domain.model.CloseoutRecord
import dev.ml.portablepos.domain.repository.CloseoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloseoutRepositoryImpl @Inject constructor(
    private val closeoutDao: CloseoutDao
) : CloseoutRepository {

    override fun getAllCloseouts(): Flow<List<CloseoutRecord>> =
        closeoutDao.getAllCloseouts().map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getCloseoutById(id: Long): CloseoutRecord? =
        closeoutDao.getCloseoutById(id)?.toDomainModel()

    override suspend fun getLatestCloseoutByCashier(cashierId: Long): CloseoutRecord? =
        closeoutDao.getLatestCloseoutByCashier(cashierId)?.toDomainModel()

    override suspend fun getTodayCloseoutByCashier(cashierName: String, startOfDay: Long, endOfDay: Long): CloseoutRecord? =
        closeoutDao.getTodayCloseoutByCashier(cashierName, startOfDay, endOfDay)?.toDomainModel()

    override suspend fun createCloseout(closeout: CloseoutRecord): Long =
        closeoutDao.insertCloseout(closeout.toEntity())
}