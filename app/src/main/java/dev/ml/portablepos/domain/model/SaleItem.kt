package dev.ml.portablepos.domain.model

data class SaleItem(
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val barcode: String? = null,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val refundedQuantity: Int = 0
)
