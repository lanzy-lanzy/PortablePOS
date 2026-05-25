package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.ProductRepository
import javax.inject.Inject

sealed class BarcodeScanResult {
    data class ProductFound(val product: Product) : BarcodeScanResult()
    data class ProductNotFound(val barcode: String) : BarcodeScanResult()
    data class Error(val message: String) : BarcodeScanResult()
}

class ProcessBarcodeScanUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): BarcodeScanResult {
        return try {
            val normalizedBarcode = barcode.trim()
            if (normalizedBarcode.isBlank()) {
                return BarcodeScanResult.Error("Barcode cannot be empty")
            }

            val product = productRepository.getProductByBarcode(normalizedBarcode)
            if (product != null) {
                BarcodeScanResult.ProductFound(product)
            } else {
                BarcodeScanResult.ProductNotFound(normalizedBarcode)
            }
        } catch (e: Exception) {
            BarcodeScanResult.Error(e.message ?: "Unknown error occurred")
        }
    }
}
