package dev.ml.portablepos.presentation.reports
import dev.ml.portablepos.util.formatAmount

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.SaleItem
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.repository.SaleRepository
import dev.ml.portablepos.domain.usecase.GetProfitReportUseCase
import dev.ml.portablepos.domain.usecase.GetReorderSuggestionsUseCase
import dev.ml.portablepos.domain.usecase.GetSalesReportUseCase
import dev.ml.portablepos.domain.usecase.ProfitReportData
import dev.ml.portablepos.domain.usecase.ReorderSuggestion
import dev.ml.portablepos.domain.usecase.SalesReportData
import dev.ml.portablepos.util.ExportUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ReportPeriod { DAILY, MONTHLY, YEARLY }

data class ReportsUiState(
    val reportData: SalesReportData? = null,
    val profitData: ProfitReportData? = null,
    val reorderSuggestions: List<ReorderSuggestion> = emptyList(),
    val bestSellingProducts: List<SaleItem> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val selectedPeriod: ReportPeriod = ReportPeriod.DAILY,
    val isExporting: Boolean = false,
    val isExportingCsv: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSalesReportUseCase: GetSalesReportUseCase,
    private val getProfitReportUseCase: GetProfitReportUseCase,
    private val getReorderSuggestionsUseCase: GetReorderSuggestionsUseCase,
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun setPeriod(period: ReportPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                launch {
                    getSalesReportUseCase()
                        .catch { e -> _uiState.update { it.copy(error = e.message ?: "Failed to load reports") } }
                        .collect { data -> _uiState.update { it.copy(reportData = data) } }
                }
                launch {
                    val now = Calendar.getInstance()
                    val (start, end) = getPeriodRange(now, _uiState.value.selectedPeriod)
                    try {
                        val profitData = getProfitReportUseCase(start, end)
                        _uiState.update { it.copy(profitData = profitData) }
                    } catch (_: Exception) {}
                }
                launch {
                    try {
                        val suggestions = getReorderSuggestionsUseCase()
                        _uiState.update { it.copy(reorderSuggestions = suggestions) }
                    } catch (_: Exception) {}
                }
                launch {
                    saleRepository.getBestSellingProducts(10)
                        .catch { e -> _uiState.update { it.copy(error = e.message ?: "Failed to load best sellers") } }
                        .collect { products -> _uiState.update { it.copy(bestSellingProducts = products) } }
                }
                launch {
                    productRepository.getLowStockProducts()
                        .catch { e -> _uiState.update { it.copy(error = e.message ?: "Failed to load low stock") } }
                        .collect { products -> _uiState.update { it.copy(lowStockProducts = products, isLoading = false) } }
                }
                launch {
                    productRepository.getOutOfStockProducts()
                        .catch { e -> _uiState.update { it.copy(error = e.message ?: "Failed to load out of stock") } }
                        .collect { outOfStock ->
                            _uiState.update { state ->
                                val existing = state.lowStockProducts.toMutableList()
                                val existingIds = existing.map { it.id }.toSet()
                                val newOnes = outOfStock.filter { it.id !in existingIds }
                                state.copy(lowStockProducts = existing + newOnes)
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load reports") }
            }
        }
    }

    fun retry() { loadReports() }

    fun exportSalesCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExportingCsv = true) }
            try {
                val now = Calendar.getInstance()
                val (startDate, endDate) = getPeriodRange(now, _uiState.value.selectedPeriod)
                val sales = saleRepository.getSalesByDateRange(startDate, endDate).first()
                val itemsMap = mutableMapOf<Long, List<SaleItem>>()
                for (sale in sales) {
                    itemsMap[sale.id] = runCatching { saleRepository.getSaleItems(sale.id).first() }.getOrDefault(emptyList())
                }
                val uri = ExportUtil.exportSalesToCsv(context, sales) { saleId ->
                    itemsMap[saleId] ?: emptyList()
                }
                if (uri != null) {
                    _uiState.update { it.copy(error = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to export CSV") }
            } finally {
                _uiState.update { it.copy(isExportingCsv = false) }
            }
        }
    }

    fun exportPdf(uri: Uri) {
        val period = _uiState.value.selectedPeriod
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val now = Calendar.getInstance()
                val (startDate, endDate) = getPeriodRange(now, period)
                val sales = saleRepository.getSalesByDateRange(startDate, endDate).first()
                val saleItems = mutableListOf<Pair<Sale, List<SaleItem>>>()
                for (sale in sales) {
                    val items = saleRepository.getSaleItems(sale.id).first()
                    saleItems.add(sale to items)
                }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        generatePdf(saleItems, startDate, endDate, period, outputStream)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to export PDF") }
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    private fun getPeriodRange(now: Calendar, period: ReportPeriod): Pair<Long, Long> {
        val start = now.clone() as Calendar
        val end = now.clone() as Calendar
        when (period) {
            ReportPeriod.DAILY -> {
                start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
                end.set(Calendar.HOUR_OF_DAY, 23); end.set(Calendar.MINUTE, 59); end.set(Calendar.SECOND, 59); end.set(Calendar.MILLISECOND, 999)
            }
            ReportPeriod.MONTHLY -> {
                start.set(Calendar.DAY_OF_MONTH, 1); start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH)); end.set(Calendar.HOUR_OF_DAY, 23); end.set(Calendar.MINUTE, 59); end.set(Calendar.SECOND, 59); end.set(Calendar.MILLISECOND, 999)
            }
            ReportPeriod.YEARLY -> {
                start.set(Calendar.DAY_OF_YEAR, 1); start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
                end.set(Calendar.DAY_OF_YEAR, end.getActualMaximum(Calendar.DAY_OF_YEAR)); end.set(Calendar.HOUR_OF_DAY, 23); end.set(Calendar.MINUTE, 59); end.set(Calendar.SECOND, 59); end.set(Calendar.MILLISECOND, 999)
            }
        }
        return start.timeInMillis to end.timeInMillis
    }

    private fun generatePdf(
        salesWithItems: List<Pair<Sale, List<SaleItem>>>,
        startDate: Long, endDate: Long, period: ReportPeriod, outputStream: OutputStream
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply { color = Color.parseColor("#1565C0"); textSize = 28f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val subtitlePaint = Paint().apply { color = Color.DKGRAY; textSize = 12f; isAntiAlias = true }
        val headerPaint = Paint().apply { color = Color.WHITE; textSize = 11f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val textPaint = Paint().apply { color = Color.DKGRAY; textSize = 10f; isAntiAlias = true }
        val boldTextPaint = Paint().apply { color = Color.DKGRAY; textSize = 10f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val labelPaint = Paint().apply { color = Color.GRAY; textSize = 10f; isAntiAlias = true }
        val dividerPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val headerBgPaint = Paint().apply { color = Color.parseColor("#1565C0") }

        var y = 40f; val margin = 40f; val pageWidth = 515f
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

        canvas.drawText("PortablePOS", margin, y, titlePaint); y += 22f
        canvas.drawText("Sales Report — ${period.name.lowercase().replaceFirstChar { it.uppercase() }}", margin, y, subtitlePaint); y += 14f
        canvas.drawText("${dateFormat.format(Date(startDate))} — ${dateFormat.format(Date(endDate))}", margin, y, subtitlePaint); y += 14f
        canvas.drawText("Generated: ${dateTimeFormat.format(Date())}", margin, y, subtitlePaint); y += 20f
        canvas.drawLine(margin, y, margin + pageWidth, y, dividerPaint); y += 16f

        val totalSales = salesWithItems.sumOf { it.first.totalAmount }
        val totalGross = salesWithItems.sumOf { it.first.subtotal }
        val totalDiscounts = salesWithItems.sumOf { it.first.discount }
        val totalTransactions = salesWithItems.size
        val totalItems = salesWithItems.sumOf { pair -> pair.second.sumOf { it.quantity } }

        canvas.drawText("SUMMARY", margin, y, boldTextPaint); y += 16f

        fun drawMetric(label: String, value: String, yPos: Float): Float {
            var cy = yPos; canvas.drawText(label, margin + 10f, cy, labelPaint); canvas.drawText(value, margin + 250f, cy, boldTextPaint); cy += 14f; return cy
        }

        y = drawMetric("Total Transactions", "$totalTransactions", y)
        y = drawMetric("Total Items Sold", "$totalItems", y)
        y = drawMetric("Gross Sales", "${formatAmount(totalGross)}", y)
        y = drawMetric("Total Discounts", "${formatAmount(totalDiscounts)}", y)
        y = drawMetric("Net Sales", "${formatAmount(totalSales)}", y); y += 16f
        canvas.drawLine(margin, y, margin + pageWidth, y, dividerPaint); y += 16f

        canvas.drawText("TRANSACTIONS", margin, y, boldTextPaint); y += 18f
        canvas.drawRect(margin, y - 2f, margin + pageWidth, y + 14f, headerBgPaint)
        canvas.drawText("#", margin + 4f, y + 10f, headerPaint)
        canvas.drawText("Transaction No.", margin + 20f, y + 10f, headerPaint)
        canvas.drawText("Date", margin + 200f, y + 10f, headerPaint)
        canvas.drawText("Items", margin + 320f, y + 10f, headerPaint)
        canvas.drawText("Total", margin + 440f, y + 10f, headerPaint); y += 22f

        val transactionDateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        salesWithItems.forEachIndexed { index, (sale, items) ->
            if (y > 800f) { document.finishPage(page); return@forEachIndexed }
            val bgPaint = if (index % 2 == 0) Paint().apply { color = Color.parseColor("#F5F5F5") } else null
            if (bgPaint != null) canvas.drawRect(margin, y - 1f, margin + pageWidth, y + 13f, bgPaint)
            canvas.drawText("${index + 1}", margin + 4f, y + 10f, textPaint)
            canvas.drawText(sale.transactionNumber, margin + 20f, y + 10f, textPaint)
            canvas.drawText(transactionDateFormat.format(Date(sale.createdAt)), margin + 200f, y + 10f, textPaint)
            canvas.drawText("${items.sumOf { it.quantity }}", margin + 325f, y + 10f, textPaint)
            canvas.drawText("${formatAmount(sale.totalAmount)}", margin + 430f, y + 10f, boldTextPaint); y += 14f
        }; y += 16f
        canvas.drawLine(margin, y, margin + pageWidth, y, dividerPaint); y += 16f
        canvas.drawText("Total: ${formatAmount(totalSales)}", margin + pageWidth - 150f, y, boldTextPaint.apply { textSize = 12f })
        document.finishPage(page); document.writeTo(outputStream); document.close()
    }
}