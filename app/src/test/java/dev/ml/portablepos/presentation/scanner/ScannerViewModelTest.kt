package dev.ml.portablepos.presentation.scanner

import dev.ml.portablepos.MainDispatcherRule
import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.model.ScannerMode
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.usecase.ProcessBarcodeScanUseCase
import dev.ml.portablepos.presentation.pos.CartManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun saleScanAddsFoundProductToCartAndGoesBack() = runTest {
        val product = testProduct(barcode = "4800000012345")
        val cartManager = CartManager()
        val viewModel = ScannerViewModel(
            processBarcodeScanUseCase = ProcessBarcodeScanUseCase(FakeProductRepository(product)),
            cartManager = cartManager
        )
        val navigationEvents = mutableListOf<ScannerNavigation>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigation.toList(navigationEvents)
        }

        viewModel.setMode(ScannerMode.SALE.name)
        viewModel.processBarcode("4800000012345")
        advanceUntilIdle()

        assertEquals(1, cartManager.items.value.size)
        assertEquals(product, cartManager.items.value.single().product)
        assertTrue(navigationEvents.single() is ScannerNavigation.GoBack)

        collectJob.cancel()
    }

    private class FakeProductRepository(
        private val product: Product?
    ) : ProductRepository {
        override fun getAllProducts(): Flow<List<Product>> = emptyFlow()
        override suspend fun getProductById(id: Long): Product? = product?.takeIf { it.id == id }
        override suspend fun getProductByIdOnce(id: Long): Product? = getProductById(id)
        override suspend fun getProductByBarcode(barcode: String): Product? = product?.takeIf { it.barcode == barcode }
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
    id = 10L,
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
