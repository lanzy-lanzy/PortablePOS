package dev.ml.portablepos.data.repository

import dev.ml.portablepos.data.local.dao.VoidedSaleDao
import dev.ml.portablepos.data.mapper.toDomainModel
import dev.ml.portablepos.data.mapper.toEntity
import dev.ml.portablepos.domain.model.VoidedSale
import dev.ml.portablepos.domain.repository.VoidedSaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoidedSaleRepositoryImpl @Inject constructor(
    private val voidedSaleDao: VoidedSaleDao
) : VoidedSaleRepository {

    override fun getAllVoidedSales(): Flow<List<VoidedSale>> =
        voidedSaleDao.getAllVoidedSales().map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getVoidedSaleBySaleId(saleId: Long): VoidedSale? =
        voidedSaleDao.getVoidedSaleBySaleId(saleId)?.toDomainModel()

    override suspend fun recordVoidedSale(voidedSale: VoidedSale): Long =
        voidedSaleDao.insertVoidedSale(voidedSale.toEntity())
}