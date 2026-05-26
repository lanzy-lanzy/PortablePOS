package dev.ml.portablepos.domain.model

data class ReturnRecord(
    val id: Long = 0,
    val saleId: Long,
    val refundAmount: Double,
    val reason: String = "",
    val processedBy: String = "Cashier",
    val returnedItemsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)
