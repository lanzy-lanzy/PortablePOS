package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.StockMovement
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.repository.StockMovementRepository
import javax.inject.Inject

class AdjustStockUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val stockMovementRepository: StockMovementRepository
) {
    suspend operator fun invoke(
        productId: Long,
        newStock: Int,
        reason: String? = null,
        createdBy: String = "Cashier"
    ): Result<Unit> {
        return try {
            if (newStock < 0) {
                return Result.failure(IllegalArgumentException("Stock quantity cannot be negative"))
            }

            val product = productRepository.getProductByIdOnce(productId)
                ?: return Result.failure(IllegalStateException("Product not found"))

            val previousStock = product.stockQuantity
            val quantityDiff = newStock - previousStock
            val movementType = if (quantityDiff >= 0) "STOCK_IN" else "STOCK_OUT"

            productRepository.updateStock(productId, newStock)

            val movement = StockMovement(
                productId = productId,
                productName = product.name,
                movementType = movementType,
                quantity = kotlin.math.abs(quantityDiff),
                previousStock = previousStock,
                newStock = newStock,
                reason = reason,
                createdAt = System.currentTimeMillis(),
                createdBy = createdBy,
                syncStatus = "PENDING_CREATE"
            )
            stockMovementRepository.addMovement(movement)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
