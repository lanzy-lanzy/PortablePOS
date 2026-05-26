package dev.ml.portablepos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_records")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    @ColumnInfo(name = "total_purchases")
    val totalPurchases: Double = 0.0,
    @ColumnInfo(name = "visit_count")
    val visitCount: Int = 0,
    @ColumnInfo(name = "last_visit")
    val lastVisit: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)