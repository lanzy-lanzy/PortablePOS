package dev.ml.portablepos.data.repository

import dev.ml.portablepos.data.local.dao.CashierDao
import dev.ml.portablepos.data.mapper.toDomainModel
import dev.ml.portablepos.data.mapper.toEntity
import dev.ml.portablepos.domain.model.Cashier
import dev.ml.portablepos.domain.repository.CashierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashierRepositoryImpl @Inject constructor(
    private val cashierDao: CashierDao
) : CashierRepository {

    override fun getAllCashiers(): Flow<List<Cashier>> {
        return cashierDao.getAllCashiers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCashierById(id: Long): Flow<Cashier?> {
        return cashierDao.getCashierById(id).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun getCashierByIdOnce(id: Long): Cashier? {
        return cashierDao.getCashierByIdOnce(id)?.toDomainModel()
    }

    override suspend fun getCashierByUsername(username: String): Cashier? {
        return cashierDao.getCashierByUsername(username)?.toDomainModel()
    }

    override suspend fun addCashier(cashier: Cashier): Long {
        return cashierDao.insertCashier(cashier.toEntity())
    }

    override suspend fun updateCashier(cashier: Cashier) {
        cashierDao.updateCashier(cashier.toEntity())
    }

    override suspend fun deleteCashier(cashier: Cashier) {
        cashierDao.deleteCashier(cashier.toEntity())
    }
}
