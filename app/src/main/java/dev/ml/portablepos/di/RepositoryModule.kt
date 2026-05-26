package dev.ml.portablepos.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ml.portablepos.data.repository.CashierRepositoryImpl
import dev.ml.portablepos.data.repository.CategoryRepositoryImpl
import dev.ml.portablepos.data.repository.CloseoutRepositoryImpl
import dev.ml.portablepos.data.repository.CustomerRepositoryImpl
import dev.ml.portablepos.data.repository.ProductRepositoryImpl
import dev.ml.portablepos.data.repository.SaleRepositoryImpl
import dev.ml.portablepos.data.repository.StockMovementRepositoryImpl
import dev.ml.portablepos.data.repository.VoidedSaleRepositoryImpl
import dev.ml.portablepos.domain.repository.CashierRepository
import dev.ml.portablepos.domain.repository.CategoryRepository
import dev.ml.portablepos.domain.repository.CloseoutRepository
import dev.ml.portablepos.domain.repository.CustomerRepository
import dev.ml.portablepos.domain.repository.ProductRepository
import dev.ml.portablepos.domain.repository.SaleRepository
import dev.ml.portablepos.domain.repository.StockMovementRepository
import dev.ml.portablepos.domain.repository.VoidedSaleRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindSaleRepository(impl: SaleRepositoryImpl): SaleRepository

    @Binds
    @Singleton
    abstract fun bindCashierRepository(impl: CashierRepositoryImpl): CashierRepository

    @Binds
    @Singleton
    abstract fun bindStockMovementRepository(impl: StockMovementRepositoryImpl): StockMovementRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindCloseoutRepository(impl: CloseoutRepositoryImpl): CloseoutRepository

    @Binds
    @Singleton
    abstract fun bindVoidedSaleRepository(impl: VoidedSaleRepositoryImpl): VoidedSaleRepository
}