package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

data class SalesReportData(
    val todaySales: Double = 0.0,
    val thisWeekSales: Double = 0.0,
    val thisMonthSales: Double = 0.0,
    val todayTransactionCount: Int = 0,
    val todayDiscounts: Double = 0.0,
    val todayGrossSales: Double = 0.0
)

class GetSalesReportUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    operator fun invoke(): Flow<SalesReportData> {
        val now = Calendar.getInstance()

        val todayStart = getDayStart(now)
        val todayEnd = getDayEnd(now)

        val weekStart = getWeekStart(now)
        val weekEnd = todayEnd

        val monthStart = getMonthStart(now)
        val monthEnd = todayEnd

        val todaySalesFlow = saleRepository.getTotalSalesToday(todayStart, todayEnd)
        val weekSalesFlow = saleRepository.getTotalSalesThisWeek(weekStart, weekEnd)
        val monthSalesFlow = saleRepository.getTotalSalesThisMonth(monthStart, monthEnd)
        val transactionCountFlow = saleRepository.getTransactionCountToday(todayStart, todayEnd)
        val discountsFlow = saleRepository.getTotalDiscounts(todayStart, todayEnd)
        val grossSalesFlow = saleRepository.getTotalGrossSales(todayStart, todayEnd)

        return combine(
            todaySalesFlow,
            weekSalesFlow,
            monthSalesFlow,
            transactionCountFlow,
            discountsFlow,
            grossSalesFlow
        ) { values: Array<out Any?> ->
            SalesReportData(
                todaySales = values[0] as Double,
                thisWeekSales = values[1] as Double,
                thisMonthSales = values[2] as Double,
                todayTransactionCount = values[3] as Int,
                todayDiscounts = values[4] as Double,
                todayGrossSales = values[5] as Double
            )
        }
    }

    private fun getDayStart(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun getDayEnd(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    private fun getWeekStart(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun getMonthStart(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
