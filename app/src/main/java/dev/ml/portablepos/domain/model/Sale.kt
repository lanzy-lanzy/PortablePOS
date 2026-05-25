package dev.ml.portablepos.domain.model

data class Sale(
    val id: Long = 0,
    val firebaseId: String? = null,
    val transactionNumber: String = "",
    val cashierName: String = "Cashier",
    val subtotal: Double,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val cashReceived: Double,
    val changeAmount: Double = 0.0,
    val paymentMethod: String = "Cash",
    val status: String = "COMPLETED",
    val syncStatus: String = "SYNCED",
    val lastSyncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
