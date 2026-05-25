package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.ProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Result<Long> {
        return try {
            val barcode = product.barcode
            if (!barcode.isNullOrBlank() && productRepository.barcodeExists(barcode)) {
                return Result.failure(IllegalArgumentException("Barcode already exists"))
            }

            if (product.sellingPrice < product.costPrice) {
                return Result.failure(
                    IllegalArgumentException("Selling price must be greater than or equal to cost price")
                )
            }

            val id = productRepository.addProduct(product)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
