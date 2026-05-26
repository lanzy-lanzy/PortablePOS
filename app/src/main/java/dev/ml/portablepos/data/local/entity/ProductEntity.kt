package dev.ml.portablepos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = true),
        Index(value = ["name"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "firebase_id")
    val firebaseId: String? = null,
    val name: String,
    val barcode: String? = null,
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,
    val description: String? = null,
    @ColumnInfo(name = "cost_price")
    val costPrice: Double = 0.0,
    @ColumnInfo(name = "base_price")
    val basePrice: Double = 0.0,
    @ColumnInfo(name = "selling_price")
    val sellingPrice: Double = 0.0,
    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Int = 0,
    @ColumnInfo(name = "reorder_level")
    val reorderLevel: Int = 0,
    val unit: String = "pcs",
    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,
    @ColumnInfo(name = "firebase_image_url")
    val firebaseImageUrl: String? = null,
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED",
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
