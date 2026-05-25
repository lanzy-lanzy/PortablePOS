package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.StockMovementEntity
import dev.ml.portablepos.domain.model.StockMovement

fun StockMovementEntity.toDomainModel(): StockMovement {
    return StockMovement(
        id = id,
        firebaseId = firebaseId,
        productId = productId,
        productName = productName,
        movementType = movementType,
        quantity = quantity,
        previousStock = previousStock,
        newStock = newStock,
        reason = reason,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt,
        createdBy = createdBy
    )
}

fun StockMovement.toEntity(): StockMovementEntity {
    return StockMovementEntity(
        id = id,
        firebaseId = firebaseId,
        productId = productId,
        productName = productName,
        movementType = movementType,
        quantity = quantity,
        previousStock = previousStock,
        newStock = newStock,
        reason = reason,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt,
        createdBy = createdBy
    )
}
