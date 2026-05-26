package dev.ml.portablepos.data.repository

import dev.ml.portablepos.data.local.dao.ProductDao
import dev.ml.portablepos.data.local.dao.ReturnRecordDao
import dev.ml.portablepos.data.local.dao.SaleDao
import dev.ml.portablepos.data.local.dao.SaleItemDao
import dev.ml.portablepos.data.mapper.toDomainModel
import dev.ml.portablepos.data.mapper.toEntity
import dev.ml.portablepos.domain.model.ItemReturnInput
import dev.ml.portablepos.domain.model.ReturnRecord
import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.SaleItem
import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepositoryImpl @Inject constructor(
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val productDao: ProductDao,
    private val returnRecordDao: ReturnRecordDao
) : SaleRepository {

    override fun getAllSales(): Flow<List<Sale>> =
        saleDao.getAllSales().map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getSaleById(id: Long): Sale? =
        saleDao.getSaleById(id)?.toDomainModel()

    override suspend fun getSaleByTransactionNumber(txnNumber: String): Sale? =
        saleDao.getSaleByTransactionNumber(txnNumber)?.toDomainModel()

    override fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>> =
        saleDao.getSalesByDateRange(startDate, endDate).map { entities -> entities.map { it.toDomainModel() } }

    override fun searchByTransactionNumber(query: String): Flow<List<Sale>> =
        saleDao.searchByTransactionNumber(query).map { entities -> entities.map { it.toDomainModel() } }

    override fun getTotalSalesToday(todayStart: Long, todayEnd: Long): Flow<Double> =
        saleDao.getTotalSalesToday(todayStart, todayEnd)

    override fun getTransactionCountToday(todayStart: Long, todayEnd: Long): Flow<Int> =
        saleDao.getTransactionCountToday(todayStart, todayEnd)

    override fun getTodaySales(todayStart: Long, todayEnd: Long): Flow<List<Sale>> =
        saleDao.getTodaySales(todayStart, todayEnd).map { entities -> entities.map { it.toDomainModel() } }

    override fun getTotalSalesThisWeek(weekStart: Long, weekEnd: Long): Flow<Double> =
        saleDao.getTotalSalesThisWeek(weekStart, weekEnd)

    override fun getTotalSalesThisMonth(monthStart: Long, monthEnd: Long): Flow<Double> =
        saleDao.getTotalSalesThisMonth(monthStart, monthEnd)

    override fun getTotalDiscounts(todayStart: Long, todayEnd: Long): Flow<Double> =
        saleDao.getTotalDiscounts(todayStart, todayEnd)

    override fun getTotalGrossSales(todayStart: Long, todayEnd: Long): Flow<Double> =
        saleDao.getTotalGrossSales(todayStart, todayEnd)

    override suspend fun insertSale(sale: Sale): Long =
        saleDao.insertSale(sale.toEntity())

    override fun getSaleItems(saleId: Long): Flow<List<SaleItem>> =
        saleItemDao.getItemsBySaleId(saleId).map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun updateSale(sale: Sale) =
        saleDao.updateSale(sale.toEntity())

    override suspend fun deleteSale(sale: Sale) =
        saleDao.deleteSale(sale.toEntity())

    override suspend fun completeSale(
        sale: Sale,
        saleItems: List<SaleItem>,
        productStocks: Map<Long, Int>
    ): Long {
        val saleId = saleDao.insertSale(sale.toEntity())
        saleItemDao.insertSaleItems(saleItems.map { it.copy(saleId = saleId).toEntity() })
        productStocks.forEach { (productId, newStock) ->
            productDao.updateStock(productId, newStock)
        }
        return saleId
    }

    override fun getReturnableItems(saleId: Long): Flow<List<SaleItem>> =
        saleItemDao.getItemsBySaleId(saleId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getAllReturnRecords(): Flow<List<ReturnRecord>> =
        returnRecordDao.getAll().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override suspend fun processReturn(saleId: Long, items: List<ItemReturnInput>, reason: String): Long {
        val totalRefund = items.sumOf { it.refundAmount }
        val record = dev.ml.portablepos.data.local.entity.ReturnRecordEntity(
            saleId = saleId,
            refundAmount = totalRefund,
            reason = reason,
            returnedItemsJson = items.toString()
        )
        val recordId = returnRecordDao.insert(record)

        val sale = saleDao.getSaleById(saleId) ?: return recordId
        saleDao.updateSale(sale.copy(refundedAmount = sale.refundedAmount + totalRefund))

        val saleItemEntities = saleItemDao.getItemsBySaleId(saleId).first()
        for (input in items) {
            val entity = saleItemEntities.find { it.productId == input.productId } ?: continue
            saleItemDao.insertSaleItem(entity.copy(refundedQuantity = entity.refundedQuantity + input.quantity))

            val product = productDao.getProductByIdOnce(input.productId) ?: continue
            productDao.updateStock(input.productId, product.stockQuantity + input.quantity)
        }

        return recordId
    }
}
