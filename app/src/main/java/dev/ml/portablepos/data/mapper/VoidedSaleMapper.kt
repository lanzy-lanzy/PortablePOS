package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.VoidedSaleEntity
import dev.ml.portablepos.domain.model.VoidedSale

fun VoidedSaleEntity.toDomainModel(): VoidedSale {
    return VoidedSale(
        id = id, saleId = saleId,
        originalTransactionNumber = originalTransactionNumber,
        originalTotal = originalTotal, reason = reason,
        voidedBy = voidedBy, voidedAt = voidedAt
    )
}

fun VoidedSale.toEntity(): VoidedSaleEntity {
    return VoidedSaleEntity(
        id = id, saleId = saleId,
        originalTransactionNumber = originalTransactionNumber,
        originalTotal = originalTotal, reason = reason,
        voidedBy = voidedBy, voidedAt = voidedAt
    )
}