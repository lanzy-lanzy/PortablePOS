package dev.ml.portablepos.data.repository

import dev.ml.portablepos.data.local.dao.ProductDao
import dev.ml.portablepos.data.local.dao.StockMovementDao
import dev.ml.portablepos.data.mapper.toDomainModel
import dev.ml.portablepos.data.mapper.toEntity
import dev.ml.portablepos.domain.model.StockMovement
import dev.ml.portablepos.domain.repository.StockMovementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockMovementRepositoryImpl @Inject constructor(
    private val stockMovementDao: StockMovementDao,
    private val productDao: ProductDao
) : StockMovementRepository {

    override fun getMovementsByProductId(productId: Long): Flow<List<StockMovement>> {
        return stockMovementDao.getMovementsByProductId(productId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllMovements(): Flow<List<StockMovement>> {
        return stockMovementDao.getAllMovements().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addMovement(movement: StockMovement) {
        stockMovementDao.insertMovement(movement.toEntity())
    }
}
