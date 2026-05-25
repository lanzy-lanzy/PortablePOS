package dev.ml.portablepos.domain.repository

import dev.ml.portablepos.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun getProductByIdOnce(id: Long): Product?
    suspend fun getProductByBarcode(barcode: String): Product?
    fun searchProducts(query: String): Flow<List<Product>>
    fun getLowStockProducts(): Flow<List<Product>>
    fun getOutOfStockProducts(): Flow<List<Product>>
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>
    fun getTotalProductCount(): Flow<Int>
    suspend fun addProduct(product: Product): Long
    suspend fun insertProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun updateStock(productId: Long, newStock: Int)
    suspend fun barcodeExists(barcode: String): Boolean
}
