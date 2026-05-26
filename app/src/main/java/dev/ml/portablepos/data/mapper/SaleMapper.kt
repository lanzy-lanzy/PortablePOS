package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.SaleEntity
import dev.ml.portablepos.domain.model.Sale

fun SaleEntity.toDomainModel(): Sale {
    return Sale(
        id = id,
        firebaseId = firebaseId,
        transactionNumber = transactionNumber,
        cashierName = cashierName,
        subtotal = subtotal,
        discount = discount,
        totalAmount = totalAmount,
        cashReceived = cashReceived,
        changeAmount = changeAmount,
        paymentMethod = paymentMethod,
        paymentReference = paymentReference,
        refundedAmount = refundedAmount,
        status = status,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt
    )
}

fun Sale.toEntity(): SaleEntity {
    return SaleEntity(
        id = id,
        firebaseId = firebaseId,
        transactionNumber = transactionNumber,
        cashierName = cashierName,
        subtotal = subtotal,
        discount = discount,
        totalAmount = totalAmount,
        cashReceived = cashReceived,
        changeAmount = changeAmount,
        paymentMethod = paymentMethod,
        paymentReference = paymentReference,
        refundedAmount = refundedAmount,
        status = status,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt
    )
}
