package dev.ml.portablepos.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ml.portablepos.data.local.database.AppDatabase
import dev.ml.portablepos.data.local.dao.CashierDao
import dev.ml.portablepos.data.local.dao.CategoryDao
import dev.ml.portablepos.data.local.dao.ProductDao
import dev.ml.portablepos.data.local.dao.SaleDao
import dev.ml.portablepos.data.local.dao.SaleItemDao
import dev.ml.portablepos.data.local.dao.StockMovementDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideSaleDao(database: AppDatabase): SaleDao = database.saleDao()

    @Provides
    fun provideSaleItemDao(database: AppDatabase): SaleItemDao = database.saleItemDao()

    @Provides
    fun provideStockMovementDao(database: AppDatabase): StockMovementDao = database.stockMovementDao()

    @Provides
    fun provideCashierDao(database: AppDatabase): CashierDao = database.cashierDao()
}
