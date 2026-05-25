package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.ProductRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Result<Unit> {
        return try {
            val existing = productRepository.getProductByIdOnce(product.id)
            if (existing == null) {
                return Result.failure(IllegalArgumentException("Product not found"))
            }

            if (product.sellingPrice < product.costPrice) {
                return Result.failure(
                    IllegalArgumentException("Selling price must be greater than or equal to cost price")
                )
            }

            val barcode = product.barcode
            if (!barcode.isNullOrBlank()) {
                val existingBarcode = productRepository.getProductByBarcode(barcode)
                if (existingBarcode != null && existingBarcode.id != product.id) {
                    return Result.failure(IllegalArgumentException("Barcode already exists"))
                }
            }

            productRepository.updateProduct(product)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
