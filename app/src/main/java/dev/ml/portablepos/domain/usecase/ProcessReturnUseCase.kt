package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.ItemReturnInput
import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ProcessReturnUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(
        saleId: Long,
        items: List<ItemReturnInput>,
        reason: String = ""
    ): Result<Long> {
        return try {
            val sale = saleRepository.getSaleById(saleId)
                ?: return Result.failure(Exception("Sale not found"))

            if (sale.status == "FULLY_REFUNDED") {
                return Result.failure(Exception("This sale has already been fully refunded"))
            }

            if (items.isEmpty()) {
                return Result.failure(Exception("No items selected for return"))
            }

            val saleItems = saleRepository.getSaleItems(saleId).first()
            for (input in items) {
                val saleItem = saleItems.find { it.productId == input.productId }
                    ?: return Result.failure(Exception("Product ${input.productId} not found in sale"))

                val returnableQty = saleItem.quantity - saleItem.refundedQuantity
                if (input.quantity <= 0 || input.quantity > returnableQty) {
                    return Result.failure(
                        Exception("Invalid return quantity for ${saleItem.productName}. Max returnable: $returnableQty")
                    )
                }

                val expectedRefund = input.quantity * saleItem.unitPrice
                if (input.refundAmount != expectedRefund) {
                    return Result.failure(
                        Exception("Refund amount mismatch for ${saleItem.productName}")
                    )
                }
            }

            Result.success(saleRepository.processReturn(saleId, items, reason))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
