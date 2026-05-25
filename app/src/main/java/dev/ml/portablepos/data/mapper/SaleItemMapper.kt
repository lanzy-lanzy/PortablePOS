package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.SaleItemEntity
import dev.ml.portablepos.domain.model.SaleItem

fun SaleItemEntity.toDomainModel(): SaleItem {
    return SaleItem(
        id = id,
        saleId = saleId,
        productId = productId,
        productName = productName,
        barcode = barcode,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice
    )
}

fun SaleItem.toEntity(): SaleItemEntity {
    return SaleItemEntity(
        id = id,
        saleId = saleId,
        productId = productId,
        productName = productName,
        barcode = barcode,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice
    )
}
