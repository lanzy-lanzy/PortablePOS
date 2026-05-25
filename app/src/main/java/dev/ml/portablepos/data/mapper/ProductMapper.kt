package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.ProductEntity
import dev.ml.portablepos.domain.model.Product

fun ProductEntity.toDomainModel(categoryName: String? = null): Product {
    return Product(
        id = id,
        firebaseId = firebaseId,
        name = name,
        barcode = barcode,
        categoryId = categoryId,
        categoryName = categoryName,
        description = description,
        costPrice = costPrice,
        sellingPrice = sellingPrice,
        stockQuantity = stockQuantity,
        reorderLevel = reorderLevel,
        unit = unit,
        imagePath = imagePath,
        firebaseImageUrl = firebaseImageUrl,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        firebaseId = firebaseId,
        name = name,
        barcode = barcode,
        categoryId = categoryId,
        description = description,
        costPrice = costPrice,
        sellingPrice = sellingPrice,
        stockQuantity = stockQuantity,
        reorderLevel = reorderLevel,
        unit = unit,
        imagePath = imagePath,
        firebaseImageUrl = firebaseImageUrl,
        syncStatus = syncStatus,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
