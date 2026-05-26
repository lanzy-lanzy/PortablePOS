package dev.ml.portablepos.domain.repository

import dev.ml.portablepos.domain.model.VoidedSale
import kotlinx.coroutines.flow.Flow

interface VoidedSaleRepository {
    fun getAllVoidedSales(): Flow<List<VoidedSale>>
    suspend fun getVoidedSaleBySaleId(saleId: Long): VoidedSale?
    suspend fun recordVoidedSale(voidedSale: VoidedSale): Long
}