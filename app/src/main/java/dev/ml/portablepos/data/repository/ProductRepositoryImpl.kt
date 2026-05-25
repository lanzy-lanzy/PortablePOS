package dev.ml.portablepos.data.repository

import dev.ml.portablepos.data.local.dao.ProductDao
import dev.ml.portablepos.data.mapper.toDomainModel
import dev.ml.portablepos.data.mapper.toEntity
import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> =
        productDao.getProducts().map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getProductById(id: Long): Product? =
        productDao.getProductById(id).first()?.toDomainModel()

    override suspend fun getProductByIdOnce(id: Long): Product? =
        productDao.getProductByIdOnce(id)?.toDomainModel()

    override suspend fun getProductByBarcode(barcode: String): Product? =
        productDao.getProductByBarcode(barcode)?.toDomainModel()

    override fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query).map { entities -> entities.map { it.toDomainModel() } }

    override fun getLowStockProducts(): Flow<List<Product>> =
        productDao.getLowStockProducts().map { entities -> entities.map { it.toDomainModel() } }

    override fun getOutOfStockProducts(): Flow<List<Product>> =
        productDao.getOutOfStockProducts().map { entities -> entities.map { it.toDomainModel() } }

    override fun getProductsByCategory(categoryId: Long): Flow<List<Product>> =
        productDao.getProductsByCategory(categoryId).map { entities -> entities.map { it.toDomainModel() } }

    override fun getTotalProductCount(): Flow<Int> = productDao.getTotalProductCount()

    override suspend fun addProduct(product: Product): Long =
        productDao.insertProduct(product.toEntity())

    override suspend fun insertProduct(product: Product): Long =
        productDao.insertProduct(product.toEntity())

    override suspend fun updateProduct(product: Product) =
        productDao.updateProduct(product.toEntity())

    override suspend fun deleteProduct(product: Product) =
        productDao.deleteProduct(product.toEntity())

    override suspend fun updateStock(productId: Long, newStock: Int) =
        productDao.updateStock(productId, newStock)

    override suspend fun barcodeExists(barcode: String): Boolean =
        productDao.barcodeExists(barcode)
}
