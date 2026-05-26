package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.CartItem
import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.SaleItem
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.repository.SaleRepository
import javax.inject.Inject

class CompleteSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val transactionNumberGenerator: GenerateTransactionNumberUseCase
) {
    suspend operator fun invoke(
        cartItems: List<CartItem>,
        cashReceived: Double,
        discount: Double = 0.0,
        cashierName: String = "Cashier",
        paymentMethod: String = "Cash",
        paymentReference: String = ""
    ): Result<Long> {
        return try {
            if (cartItems.isEmpty()) {
                return Result.failure(IllegalStateException("Cart is empty"))
            }

            if (discount < 0) {
                return Result.failure(IllegalArgumentException("Discount cannot be negative"))
            }

            val subtotal = cartItems.sumOf { it.subtotal }

            if (discount > subtotal) {
                return Result.failure(IllegalArgumentException("Discount cannot exceed subtotal"))
            }

            val totalAmount = subtotal - discount

            if (cashReceived < totalAmount) {
                return Result.failure(
                    IllegalArgumentException("Cash received must be greater than or equal to total amount")
                )
            }

            val productStocks = mutableMapOf<Long, Int>()
            for (item in cartItems) {
                val product = productRepository.getProductByIdOnce(item.product.id)
                    ?: return Result.failure(
                        IllegalStateException("Product not found: ${item.product.name}")
                    )

                if (product.stockQuantity < item.quantity) {
                    return Result.failure(
                        IllegalArgumentException("Insufficient stock for ${product.name}. Available: ${product.stockQuantity}, Requested: ${item.quantity}")
                    )
                }

                productStocks[item.product.id] = product.stockQuantity - item.quantity
            }

            val transactionNumber = transactionNumberGenerator()

            val saleItems = cartItems.map { item ->
                SaleItem(
                    productId = item.product.id,
                    productName = item.product.name,
                    barcode = item.product.barcode,
                    quantity = item.quantity,
                    unitPrice = item.product.sellingPrice,
                    totalPrice = item.subtotal,
                    saleId = 0L
                )
            }

            val sale = Sale(
                transactionNumber = transactionNumber,
                cashierName = cashierName,
                subtotal = subtotal,
                discount = discount,
                totalAmount = totalAmount,
                cashReceived = cashReceived,
                changeAmount = cashReceived - totalAmount,
                paymentMethod = paymentMethod,
                paymentReference = paymentReference,
                status = "COMPLETED",
                syncStatus = "PENDING_CREATE"
            )

            val saleId = saleRepository.completeSale(
                sale = sale,
                saleItems = saleItems,
                productStocks = productStocks,
                cashierName = cashierName
            )
            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
