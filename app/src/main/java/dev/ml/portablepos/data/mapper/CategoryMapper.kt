package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.CategoryEntity
import dev.ml.portablepos.domain.model.Category

fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        firebaseId = firebaseId,
        name = name,
        description = description,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        firebaseId = firebaseId,
        name = name,
        description = description,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
