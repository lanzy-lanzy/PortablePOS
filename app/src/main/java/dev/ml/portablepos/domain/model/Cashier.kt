package dev.ml.portablepos.domain.model

data class Cashier(
    val id: Long = 0,
    val firebaseId: String? = null,
    val fullName: String = "",
    val username: String = "",
    val role: String = "CASHIER",
    val pinCode: String = "",
    val syncStatus: String = "SYNCED",
    val lastSyncedAt: Long? = null,
    val createdAt: Long = 0L
)
