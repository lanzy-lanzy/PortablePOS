package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.model.CloseoutRecord
import dev.ml.portablepos.domain.repository.CloseoutRepository
import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateCloseoutUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val closeoutRepository: CloseoutRepository
) {
    suspend operator fun invoke(
        cashierId: Long?,
        cashierName: String,
        actualCash: Double
    ): Result<Long> {
        return try {
            val now = System.currentTimeMillis()
            val todayStart = getDayStart(now)
            val todayEnd = getDayEnd(now)

            val todaySales = saleRepository.getTodaySales(todayStart, todayEnd).first()
            val todaySalesTotal = saleRepository.getTotalSalesToday(todayStart, todayEnd).first()
            val todayReturns = saleRepository.getTotalReturns(todayStart, todayEnd).first()
            val transactionCount = saleRepository.getTransactionCountToday(todayStart, todayEnd).first()

            val previousCloseout = closeoutRepository.getTodayCloseoutByCashier(
                cashierName, todayStart, todayEnd
            )

            val openingBalance = previousCloseout?.closingBalance ?: 0.0
            val expectedCash = openingBalance + todaySalesTotal - todayReturns

            val closeout = CloseoutRecord(
                cashierId = cashierId,
                cashierName = cashierName,
                openingBalance = openingBalance,
                closingBalance = actualCash,
                expectedCash = expectedCash,
                actualCash = actualCash,
                difference = actualCash - expectedCash,
                totalSales = todaySalesTotal,
                totalReturns = todayReturns,
                transactionCount = transactionCount,
                closedAt = now,
                createdAt = now
            )

            val id = closeoutRepository.createCloseout(closeout)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getDayStart(millis: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getDayEnd(millis: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}