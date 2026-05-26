package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.StockMovement
import dev.ml.portablepos.domain.model.VoidedSale
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.repository.SaleRepository
import dev.ml.portablepos.domain.repository.StockMovementRepository
import dev.ml.portablepos.domain.repository.VoidedSaleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class VoidSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val stockMovementRepository: StockMovementRepository,
    private val voidedSaleRepository: VoidedSaleRepository
) {
    suspend operator fun invoke(
        saleId: Long,
        reason: String,
        voidedBy: String
    ): Result<Unit> {
        return try {
            val sale = saleRepository.getSaleById(saleId)
                ?: return Result.failure(IllegalStateException("Sale not found"))

            if (sale.status == "VOIDED") {
                return Result.failure(IllegalStateException("Sale is already voided"))
            }

            if (reason.isBlank()) {
                return Result.failure(IllegalArgumentException("Void reason is required"))
            }

            val saleItems = saleRepository.getSaleItems(saleId).first()

            // Restore stock for each item
            for (item in saleItems) {
                val product = productRepository.getProductByIdOnce(item.productId) ?: continue
                val previousStock = product.stockQuantity
                val newStock = previousStock + item.quantity
                productRepository.updateStock(item.productId, newStock)
                stockMovementRepository.addMovement(
                    StockMovement(
                        productId = item.productId,
                        productName = item.productName,
                        movementType = "RETURN",
                        quantity = item.quantity,
                        previousStock = previousStock,
                        newStock = newStock,
                        reason = "Void sale #${sale.transactionNumber}: $reason",
                        createdAt = System.currentTimeMillis(),
                        createdBy = voidedBy,
                        syncStatus = "PENDING_CREATE"
                    )
                )
            }

            // Mark sale as voided
            saleRepository.updateSale(sale.copy(status = "VOIDED"))

            // Record void audit trail
            voidedSaleRepository.recordVoidedSale(
                VoidedSale(
                    saleId = saleId,
                    originalTransactionNumber = sale.transactionNumber,
                    originalTotal = sale.totalAmount,
                    reason = reason,
                    voidedBy = voidedBy
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}