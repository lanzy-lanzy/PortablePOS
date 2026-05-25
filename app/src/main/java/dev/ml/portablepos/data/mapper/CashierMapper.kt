package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.CashierEntity
import dev.ml.portablepos.domain.model.Cashier

fun CashierEntity.toDomainModel(): Cashier {
    return Cashier(
        id = id,
        firebaseId = firebaseId,
        fullName = fullName,
        username = username,
        role = role,
        pinCode = pinCode,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt
    )
}

fun Cashier.toEntity(): CashierEntity {
    return CashierEntity(
        id = id,
        firebaseId = firebaseId,
        fullName = fullName,
        username = username,
        role = role,
        pinCode = pinCode,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt
    )
}
