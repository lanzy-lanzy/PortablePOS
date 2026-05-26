package dev.ml.portablepos.data.mapper

import dev.ml.portablepos.data.local.entity.CustomerEntity
import dev.ml.portablepos.domain.model.Customer

fun CustomerEntity.toDomainModel(): Customer {
    return Customer(
        id = id, name = name, phone = phone, email = email,
        address = address, totalPurchases = totalPurchases,
        visitCount = visitCount, lastVisit = lastVisit,
        createdAt = createdAt, updatedAt = updatedAt
    )
}

fun Customer.toEntity(): CustomerEntity {
    return CustomerEntity(
        id = id, name = name, phone = phone, email = email,
        address = address, totalPurchases = totalPurchases,
        visitCount = visitCount, lastVisit = lastVisit,
        createdAt = createdAt, updatedAt = updatedAt
    )
}