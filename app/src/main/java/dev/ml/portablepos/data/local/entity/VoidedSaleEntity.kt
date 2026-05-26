package dev.ml.portablepos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voided_sales")
data class VoidedSaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "sale_id")
    val saleId: Long,
    @ColumnInfo(name = "original_transaction_number")
    val originalTransactionNumber: String = "",
    @ColumnInfo(name = "original_total")
    val originalTotal: Double = 0.0,
    val reason: String = "",
    @ColumnInfo(name = "voided_by")
    val voidedBy: String = "",
    @ColumnInfo(name = "voided_at")
    val voidedAt: Long = System.currentTimeMillis()
)