package dev.ml.portablepos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["transaction_number"], unique = true)
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "firebase_id")
    val firebaseId: String? = null,
    @ColumnInfo(name = "transaction_number")
    val transactionNumber: String,
    @ColumnInfo(name = "cashier_name")
    val cashierName: String = "Cashier",
    val subtotal: Double,
    val discount: Double = 0.0,
    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,
    @ColumnInfo(name = "cash_received")
    val cashReceived: Double,
    @ColumnInfo(name = "change_amount")
    val changeAmount: Double = 0.0,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "Cash",
    @ColumnInfo(name = "payment_reference")
    val paymentReference: String = "",
    @ColumnInfo(name = "refunded_amount")
    val refundedAmount: Double = 0.0,
    val status: String = "COMPLETED",
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED",
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
