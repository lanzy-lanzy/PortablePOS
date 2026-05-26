package dev.ml.portablepos.domain.model

data class CloseoutRecord(
    val id: Long = 0,
    val cashierId: Long? = null,
    val cashierName: String = "",
    val openingBalance: Double = 0.0,
    val closingBalance: Double = 0.0,
    val expectedCash: Double = 0.0,
    val actualCash: Double = 0.0,
    val difference: Double = 0.0,
    val totalSales: Double = 0.0,
    val totalReturns: Double = 0.0,
    val transactionCount: Int = 0,
    val notes: String? = null,
    val closedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)