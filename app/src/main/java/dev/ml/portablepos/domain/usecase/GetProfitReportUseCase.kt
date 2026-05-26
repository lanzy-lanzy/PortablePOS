package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.SaleItem
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.roundToLong

data class ProfitReportData(
    val totalRevenue: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalProfit: Double = 0.0,
    val profitMargin: Double = 0.0,
    val transactionCount: Int = 0,
    val topProfitProducts: List<ProfitItem> = emptyList()
)

data class ProfitItem(
    val productName: String,
    val quantitySold: Int,
    val revenue: Double,
    val cost: Double,
    val profit: Double
)

class GetProfitReportUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        startDate: Long,
        endDate: Long
    ): ProfitReportData {
        val sales = saleRepository.getSalesByDateRange(startDate, endDate).first()
        var totalRevenue = 0.0
        var totalCost = 0.0
        val profitMap = mutableMapOf<Long, ProfitItem>()

        for (sale in sales) {
            if (sale.status == "VOIDED") continue
            totalRevenue += sale.totalAmount

            val items = saleRepository.getSaleItems(sale.id).first()
            for (item in items) {
                val product = productRepository.getProductByIdOnce(item.productId)
                val costPrice = product?.costPrice ?: 0.0
                val itemCost = costPrice * item.quantity
                totalCost += itemCost

                val existing = profitMap[item.productId]
                if (existing != null) {
                    profitMap[item.productId] = existing.copy(
                        quantitySold = existing.quantitySold + item.quantity,
                        revenue = existing.revenue + item.totalPrice,
                        cost = existing.cost + itemCost,
                        profit = (existing.revenue + item.totalPrice) - (existing.cost + itemCost)
                    )
                } else {
                    profitMap[item.productId] = ProfitItem(
                        productName = item.productName,
                        quantitySold = item.quantity,
                        revenue = item.totalPrice,
                        cost = itemCost,
                        profit = item.totalPrice - itemCost
                    )
                }
            }
        }

        val totalProfit = totalRevenue - totalCost
        val profitMargin = if (totalRevenue > 0) (totalProfit / totalRevenue * 100).let {
            (it * 100).roundToLong() / 100.0
        } else 0.0

        return ProfitReportData(
            totalRevenue = totalRevenue,
            totalCost = totalCost,
            totalProfit = totalProfit,
            profitMargin = profitMargin,
            transactionCount = sales.size,
            topProfitProducts = profitMap.values
                .sortedByDescending { it.profit }
                .take(10)
        )
    }
}