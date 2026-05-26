package dev.ml.portablepos.domain.usecase

import dev.ml.portablepos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GenerateTransactionNumberUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(): String {
        val now = Calendar.getInstance()
        val todayStart = getDayStart(now)
        val todayEnd = getDayEnd(now)

        val todayCount = saleRepository.getTransactionCountToday(todayStart, todayEnd)
            .first()

        val datePart = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(now.timeInMillis))
        val sequence = String.format("%04d", todayCount + 1)

        return "TXN-$datePart-$sequence"
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
}
