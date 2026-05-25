package dev.ml.portablepos.data.remote.dto

/**
 * Placeholder for Firebase DTO (Data Transfer Objects).
 * These will be used for serializing/deserializing data
 * when syncing with Firebase Firestore.
 *
 * TODO: Implement Firebase DTOs when Firebase is integrated.
 */
data class SyncDto(
    val id: String = "",
    val data: Map<String, Any> = emptyMap(),
    val lastSyncedAt: Long = 0L
)
