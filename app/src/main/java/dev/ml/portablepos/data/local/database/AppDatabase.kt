package dev.ml.portablepos.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.ml.portablepos.data.local.dao.CashierDao
import dev.ml.portablepos.data.local.dao.CategoryDao
import dev.ml.portablepos.data.local.dao.ProductDao
import dev.ml.portablepos.data.local.dao.SaleDao
import dev.ml.portablepos.data.local.dao.SaleItemDao
import dev.ml.portablepos.data.local.dao.StockMovementDao
import dev.ml.portablepos.data.local.entity.CashierEntity
import dev.ml.portablepos.data.local.entity.CategoryEntity
import dev.ml.portablepos.data.local.entity.ProductEntity
import dev.ml.portablepos.data.local.entity.SaleEntity
import dev.ml.portablepos.data.local.entity.SaleItemEntity
import dev.ml.portablepos.data.local.entity.StockMovementEntity

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        StockMovementEntity::class,
        CashierEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun cashierDao(): CashierDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "portable_pos_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
