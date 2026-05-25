package dev.ml.portablepos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cashiers",
    indices = [
        Index(value = ["username"], unique = true)
    ]
)
data class CashierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "firebase_id")
    val firebaseId: String? = null,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    val username: String,
    val role: String = "CASHIER",
    @ColumnInfo(name = "pin_code")
    val pinCode: String,
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED",
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
