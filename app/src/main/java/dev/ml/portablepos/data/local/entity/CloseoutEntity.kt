package dev.ml.portablepos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "closeout_records")
data class CloseoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "cashier_id")
    val cashierId: Long? = null,
    @ColumnInfo(name = "cashier_name")
    val cashierName: String = "",
    @ColumnInfo(name = "opening_balance")
    val openingBalance: Double = 0.0,
    @ColumnInfo(name = "closing_balance")
    val closingBalance: Double = 0.0,
    @ColumnInfo(name = "expected_cash")
    val expectedCash: Double = 0.0,
    @ColumnInfo(name = "actual_cash")
    val actualCash: Double = 0.0,
    val difference: Double = 0.0,
    @ColumnInfo(name = "total_sales")
    val totalSales: Double = 0.0,
    @ColumnInfo(name = "total_returns")
    val totalReturns: Double = 0.0,
    @ColumnInfo(name = "transaction_count")
    val transactionCount: Int = 0,
    val notes: String? = null,
    @ColumnInfo(name = "closed_at")
    val closedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)