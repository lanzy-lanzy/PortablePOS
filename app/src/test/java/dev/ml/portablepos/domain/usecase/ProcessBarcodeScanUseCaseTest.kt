package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessBarcodeScanUseCaseTest {
    @Test
    fun trimsScannedBarcodeBeforeLookup() = runTest {
        val product = testProduct(barcode = "4800000012345")
        val repository = FakeProductRepository(product)
        val useCase = ProcessBarcodeScanUseCase(repository)

        val result = useCase(" 4800000012345\n")

        assertEquals("4800000012345", repository.lastLookup)
        assertTrue(result is BarcodeScanResult.ProductFound)
    }

    private class FakeProductRepository(
        private val product: Product?
    ) : ProductRepository {
        var lastLookup: String? = null

        override fun getAllProducts(): Flow<List<Product>> = emptyFlow()
        override suspend fun getProductById(id: Long): Product? = product?.takeIf { it.id == id }
        override suspend fun getProductByIdOnce(id: Long): Product? = getProductById(id)
        override suspend fun getProductByBarcode(barcode: String): Product? {
            lastLookup = barcode
            return product?.takeIf { it.barcode == barcode }
        }
        override fun searchProducts(query: String): Flow<List<Product>> = emptyFlow()
        override fun getLowStockProducts(): Flow<List<Product>> = emptyFlow()
        override fun getOutOfStockProducts(): Flow<List<Product>> = emptyFlow()
        override fun getProductsByCategory(categoryId: Long): Flow<List<Product>> = emptyFlow()
        override fun getTotalProductCount(): Flow<Int> = emptyFlow()
        override suspend fun addProduct(product: Product): Long = product.id
        override suspend fun insertProduct(product: Product): Long = product.id
        override suspend fun updateProduct(product: Product) = Unit
        override suspend fun deleteProduct(product: Product) = Unit
        override suspend fun updateStock(productId: Long, newStock: Int) = Unit
        override suspend fun barcodeExists(barcode: String): Boolean = product?.barcode == barcode
    }
}

private fun testProduct(barcode: String) = Product(
    id = 20L,
    name = "Test Product",
    barcode = barcode,
    categoryId = 1L,
    categoryName = "Test",
    costPrice = 5.0,
    sellingPrice = 10.0,
    stockQuantity = 12,
    reorderLevel = 2,
    createdAt = 1L,
    updatedAt = 1L
)
