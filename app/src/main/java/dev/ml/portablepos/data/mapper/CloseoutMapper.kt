package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.CloseoutEntity
import dev.ml.portablepos.domain.model.CloseoutRecord

fun CloseoutEntity.toDomainModel(): CloseoutRecord {
    return CloseoutRecord(
        id = id, cashierId = cashierId, cashierName = cashierName,
        openingBalance = openingBalance, closingBalance = closingBalance,
        expectedCash = expectedCash, actualCash = actualCash,
        difference = difference, totalSales = totalSales,
        totalReturns = totalReturns, transactionCount = transactionCount,
        notes = notes, closedAt = closedAt, createdAt = createdAt
    )
}

fun CloseoutRecord.toEntity(): CloseoutEntity {
    return CloseoutEntity(
        id = id, cashierId = cashierId, cashierName = cashierName,
        openingBalance = openingBalance, closingBalance = closingBalance,
        expectedCash = expectedCash, actualCash = actualCash,
        difference = difference, totalSales = totalSales,
        totalReturns = totalReturns, transactionCount = transactionCount,
        notes = notes, closedAt = closedAt, createdAt = createdAt
    )
}