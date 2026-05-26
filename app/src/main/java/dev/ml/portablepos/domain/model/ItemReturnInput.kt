package dev.ml.portablepos.domain.model

data class ItemReturnInput(
    val productId: Long,
    val quantity: Int,
    val refundAmount: Double
)
