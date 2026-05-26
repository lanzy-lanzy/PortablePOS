package dev.ml.portablepos.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val totalPurchases: Double = 0.0,
    val visitCount: Int = 0,
    val lastVisit: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)