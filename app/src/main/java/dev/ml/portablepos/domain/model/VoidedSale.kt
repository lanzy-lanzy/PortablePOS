package dev.ml.portablepos.domain.model

data class VoidedSale(
    val id: Long = 0,
    val saleId: Long,
    val originalTransactionNumber: String = "",
    val originalTotal: Double = 0.0,
    val reason: String = "",
    val voidedBy: String = "",
    val voidedAt: Long = System.currentTimeMillis()
)