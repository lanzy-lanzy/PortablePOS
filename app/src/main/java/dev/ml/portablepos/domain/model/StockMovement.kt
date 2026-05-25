package dev.ml.portablepos.domain.model

data class StockMovement(
    val id: Long = 0,
    val firebaseId: String? = null,
    val productId: Long = 0,
    val productName: String? = null,
    val movementType: String = "STOCK_IN",
    val quantity: Int = 0,
    val previousStock: Int = 0,
    val newStock: Int = 0,
    val reason: String? = null,
    val syncStatus: String = "SYNCED",
    val lastSyncedAt: Long? = null,
    val createdAt: Long = 0L,
    val createdBy: String = "Cashier"
)
