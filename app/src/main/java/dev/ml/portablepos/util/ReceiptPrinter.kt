package dev.ml.portablepos.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import dev.ml.portablepos.domain.model.Sale
import dev.ml.portablepos.domain.model.SaleItem
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ReceiptPrinter {

    private const val PRINTER_UUID = "00001101-0000-1000-8000-00805F9B34FB"

    data class ReceiptData(
        val storeName: String,
        val storeAddress: String = "",
        val storeContact: String = "",
        val footer: String = "",
        val showTax: Boolean = true,
        val taxRate: Double = 0.0
    )

    fun buildReceiptText(
        sale: Sale,
        items: List<SaleItem>,
        receiptData: ReceiptData
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sb = StringBuilder()

        sb.appendLine(receiptData.storeName.ifBlank { "PortablePOS" })
        if (receiptData.storeAddress.isNotBlank()) sb.appendLine(receiptData.storeAddress)
        if (receiptData.storeContact.isNotBlank()) sb.appendLine("Tel: ${receiptData.storeContact}")
        sb.appendLine("================================")
        sb.appendLine("TXN: ${sale.transactionNumber}")
        sb.appendLine("Date: ${dateFormat.format(Date(sale.createdAt))}")
        sb.appendLine("Cashier: ${sale.cashierName}")
        sb.appendLine("================================")
        sb.appendLine(String.format("%-25s %s", "Item", "Amount"))
        sb.appendLine("--------------------------------")

        for (item in items) {
            val line = String.format("%-25s %s", item.productName.take(25), formatAmount(item.totalPrice))
            sb.appendLine(line)
            val qtyLine = String.format("  %d x %s", item.quantity, formatAmount(item.unitPrice))
            sb.appendLine(qtyLine)
        }

        sb.appendLine("--------------------------------")
        if (receiptData.showTax && receiptData.taxRate > 0) {
            val taxable = sale.subtotal
            val vat = taxable * receiptData.taxRate / (1.0 + receiptData.taxRate)
            sb.appendLine(String.format("%-20s %s", "Subtotal:", formatAmount(sale.subtotal)))
            sb.appendLine(String.format("%-20s %s", "VAT (${(receiptData.taxRate * 100).toInt()}%):", formatAmount(vat)))
            sb.appendLine(String.format("%-20s %s", "Discount:", formatAmount(sale.discount)))
            sb.appendLine(String.format("%-20s %s", "Total:", formatAmount(sale.totalAmount)))
        } else {
            sb.appendLine(String.format("%-20s %s", "Subtotal:", formatAmount(sale.subtotal)))
            if (sale.discount > 0) {
                sb.appendLine(String.format("%-20s %s", "Discount:", formatAmount(sale.discount)))
            }
            sb.appendLine(String.format("%-20s %s", "Total:", formatAmount(sale.totalAmount)))
        }
        sb.appendLine(String.format("%-20s %s", "Cash:", formatAmount(sale.cashReceived)))
        sb.appendLine(String.format("%-20s %s", "Change:", formatAmount(sale.changeAmount)))
        sb.appendLine("================================")
        sb.appendLine("Payment: ${sale.paymentMethod}")
        if (receiptData.footer.isNotBlank()) {
            sb.appendLine(receiptData.footer)
        }
        sb.appendLine("Thank you for your purchase!")

        return sb.toString()
    }

    private fun formatAmount(amount: Double): String {
        return String.format("%.2f", amount)
    }

    fun printToBluetooth(
        deviceAddress: String,
        receiptText: String
    ): Result<Unit> {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
                ?: return Result.failure(IOException("Bluetooth not available"))

            if (!adapter.isEnabled) {
                return Result.failure(IOException("Bluetooth is not enabled"))
            }

            val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)
            val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(
                UUID.fromString(PRINTER_UUID)
            )

            adapter.cancelDiscovery()
            socket.connect()

            val outputStream: OutputStream = socket.outputStream
            // ESC/POS commands for 58mm thermal printer
            val initPrinter = byteArrayOf(0x1B, 0x40) // ESC @
            val centerAlign = byteArrayOf(0x1B, 0x61, 0x01) // ESC a 1
            val leftAlign = byteArrayOf(0x1B, 0x61, 0x00) // ESC a 0
            val boldOn = byteArrayOf(0x1B, 0x45, 0x01) // ESC E 1
            val boldOff = byteArrayOf(0x1B, 0x45, 0x00) // ESC E 0
            val cutPaper = byteArrayOf(0x1D, 0x56, 0x01) // GS V 1

            outputStream.write(initPrinter)
            outputStream.write(centerAlign)
            outputStream.write(receiptText.toByteArray(charset("CP437")))
            outputStream.write(leftAlign)
            outputStream.write(cutPaper)
            outputStream.flush()

            socket.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}