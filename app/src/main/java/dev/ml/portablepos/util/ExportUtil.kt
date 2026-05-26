package dev.ml.portablepos.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dev.ml.portablepos.domain.model.Product
import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.SaleItem
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtil {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun exportSalesToCsv(
        context: Context,
        sales: List<Sale>,
        getSaleItems: (Long) -> List<SaleItem>
    ): Uri? {
        val sb = StringBuilder()
        sb.appendLine("Transaction#,Date,Cashier,Subtotal,Discount,Total,Cash,Change,Payment,Status")
        for (sale in sales) {
            sb.appendLine(
                "${sale.transactionNumber},${dateFormat.format(Date(sale.createdAt))}," +
                        "${sale.cashierName},${sale.subtotal},${sale.discount},${sale.totalAmount}," +
                        "${sale.cashReceived},${sale.changeAmount},${sale.paymentMethod},${sale.status}"
            )
            val items = getSaleItems(sale.id)
            for (item in items) {
                sb.appendLine(",,,${item.productName},${item.quantity}x${item.unitPrice},${item.totalPrice}")
            }
        }
        return saveToFile(context, sb.toString(), "sales_export", "text/csv")
    }

    fun exportProductsToCsv(
        context: Context,
        products: List<Product>
    ): Uri? {
        val sb = StringBuilder()
        sb.appendLine("Name,Barcode,Category,Cost Price,Selling Price,Stock,Reorder Level,Unit")
        for (product in products) {
            sb.appendLine(
                "${product.name},${product.barcode ?: ""},${product.categoryName ?: ""}," +
                        "${product.costPrice},${product.sellingPrice},${product.stockQuantity}," +
                        "${product.reorderLevel},${product.unit}"
            )
        }
        return saveToFile(context, sb.toString(), "inventory_export", "text/csv")
    }

    private fun saveToFile(context: Context, content: String, prefix: String, mimeType: String): Uri? {
        val fileName = "${prefix}_${fileDateFormat.format(Date())}.csv"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
            )
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(content.toByteArray())
                }
            }
            uri
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            val file = java.io.File(dir, fileName)
            file.writeText(content)
            Uri.fromFile(file)
        }
    }
}