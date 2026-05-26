package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.SaleItem
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

data class DashboardData(
    val todaySales: Double = 0.0,
    val transactionCount: Int = 0,
    val lowStockCount: Int = 0,
    val totalProducts: Int = 0,
    val bestSellingProducts: List<SaleItem> = emptyList()
)

class GetDashboardDataUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<DashboardData> {
        val todayStart = getTodayStart()
        val todayEnd = getTodayEnd()

        val todaySalesFlow = saleRepository.getTotalSalesToday(todayStart, todayEnd)
        val transactionCountFlow = saleRepository.getTransactionCountToday(todayStart, todayEnd)
        val lowStockCountFlow = productRepository.getLowStockProducts().map { it.size }
        val totalProductsFlow = productRepository.getTotalProductCount()
        val bestSellingFlow = saleRepository.getBestSellingProducts(5)

        return combine(
            todaySalesFlow,
            transactionCountFlow,
            lowStockCountFlow,
            totalProductsFlow,
            bestSellingFlow
        ) { todaySales, transactionCount, lowStockCount, totalProducts, bestSelling ->
            DashboardData(
                todaySales = todaySales,
                transactionCount = transactionCount,
                lowStockCount = lowStockCount,
                totalProducts = totalProducts,
                bestSellingProducts = bestSelling
            )
        }
    }

    private fun getTodayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getTodayEnd(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}
