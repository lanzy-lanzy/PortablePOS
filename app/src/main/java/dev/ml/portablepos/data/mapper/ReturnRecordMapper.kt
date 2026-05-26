package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.ReturnRecordEntity
import dev.ml.portablepos.domain.model.ReturnRecord

fun ReturnRecordEntity.toDomainModel(): ReturnRecord {
    return ReturnRecord(
        id = id,
        saleId = saleId,
        refundAmount = refundAmount,
        reason = reason,
        processedBy = processedBy,
        returnedItemsJson = returnedItemsJson,
        createdAt = createdAt
    )
}

fun ReturnRecord.toEntity(): ReturnRecordEntity {
    return ReturnRecordEntity(
        id = id,
        saleId = saleId,
        refundAmount = refundAmount,
        reason = reason,
        processedBy = processedBy,
        returnedItemsJson = returnedItemsJson,
        createdAt = createdAt
    )
}
