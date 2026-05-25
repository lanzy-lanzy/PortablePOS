package dev.ml.portablepos.domain.repository

import dev.ml.portablepos.domain.model.StockMovement
import kotlinx.coroutines.flow.Flow

interface StockMovementRepository {
    fun getMovementsByProductId(productId: Long): Flow<List<StockMovement>>
    fun getAllMovements(): Flow<List<StockMovement>>
    suspend fun addMovement(movement: StockMovement)
}
