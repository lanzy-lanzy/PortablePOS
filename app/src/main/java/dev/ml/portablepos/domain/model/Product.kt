package dev.ml.portablepos.domain.model

data class Product(
    val id: Long = 0,
    val firebaseId: String? = null,
    val name: String,
    val barcode: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val description: String? = null,
    val costPrice: Double = 0.0,
    val basePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stockQuantity: Int = 0,
    val reorderLevel: Int = 0,
    val unit: String = "pcs",
    val imagePath: String? = null,
    val firebaseImageUrl: String? = null,
    val syncStatus: String = "SYNCED",
    val lastSyncedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
