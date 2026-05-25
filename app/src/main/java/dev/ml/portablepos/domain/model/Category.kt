package dev.ml.portablepos.domain.model

data class Category(
    val id: Long = 0,
    val firebaseId: String? = null,
    val name: String,
    val description: String? = null,
    val syncStatus: String = "SYNCED",
    val lastSyncedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
