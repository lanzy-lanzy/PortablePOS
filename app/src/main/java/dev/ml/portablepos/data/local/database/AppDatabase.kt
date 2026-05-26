package dev.ml.portablepos.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.ml.portablepos.domain.model.ItemReturnInput
import kotlinx.coroutines.flow.first
import dev.ml.portablepos.data.local.dao.CashierDao
import dev.ml.portablepos.data.local.dao.CategoryDao
import dev.ml.portablepos.data.local.dao.CloseoutDao
import dev.ml.portablepos.data.local.dao.CustomerDao
import dev.ml.portablepos.data.local.dao.ProductDao
import dev.ml.portablepos.data.local.dao.ReturnRecordDao
import dev.ml.portablepos.data.local.dao.SaleDao
import dev.ml.portablepos.data.local.dao.SaleItemDao
import dev.ml.portablepos.data.local.dao.StockMovementDao
import dev.ml.portablepos.data.local.dao.VoidedSaleDao
import dev.ml.portablepos.data.local.entity.CashierEntity
import dev.ml.portablepos.data.local.entity.CategoryEntity
import dev.ml.portablepos.data.local.entity.CloseoutEntity
import dev.ml.portablepos.data.local.entity.CustomerEntity
import dev.ml.portablepos.data.local.entity.ProductEntity
import dev.ml.portablepos.data.local.entity.ReturnRecordEntity
import dev.ml.portablepos.data.local.entity.SaleEntity
import dev.ml.portablepos.data.local.entity.SaleItemEntity
import dev.ml.portablepos.data.local.entity.StockMovementEntity
import dev.ml.portablepos.data.local.entity.VoidedSaleEntity

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        StockMovementEntity::class,
        CashierEntity::class,
        ReturnRecordEntity::class,
        CustomerEntity::class,
        CloseoutEntity::class,
        VoidedSaleEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun cashierDao(): CashierDao
    abstract fun returnRecordDao(): ReturnRecordDao
    abstract fun customerDao(): CustomerDao
    abstract fun closeoutDao(): CloseoutDao
    abstract fun voidedSaleDao(): VoidedSaleDao

    @Transaction
    open suspend fun completeSaleTransaction(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        productStocks: Map<Long, Int>,
        cashierName: String
    ): Long {
        val saleId = saleDao().insertSale(sale)
        saleItemDao().insertSaleItems(items.map { it.copy(saleId = saleId) })
        val now = System.currentTimeMillis()
        for ((productId, newStock) in productStocks) {
            val product = productDao().getProductByIdOnce(productId)
            productDao().updateStock(productId, newStock)
            if (product != null) {
                stockMovementDao().insertMovement(
                    StockMovementEntity(
                        productId = productId,
                        productName = product.name,
                        movementType = "SALE",
                        quantity = product.stockQuantity - newStock,
                        previousStock = product.stockQuantity,
                        newStock = newStock,
                        reason = "Sale #$saleId",
                        createdAt = now,
                        createdBy = cashierName,
                        syncStatus = "PENDING_CREATE"
                    )
                )
            }
        }
        return saleId
    }

    @Transaction
    open suspend fun processReturnTransaction(
        saleId: Long,
        items: List<dev.ml.portablepos.domain.model.ItemReturnInput>,
        reason: String
    ): Long {
        val totalRefund = items.sumOf { it.refundAmount }
        val saleItemEntities = saleItemDao().getItemsBySaleId(saleId).first()
        val now = System.currentTimeMillis()

        for (input in items) {
            val entity = saleItemEntities.find { it.productId == input.productId } ?: continue
            saleItemDao().updateSaleItem(entity.copy(refundedQuantity = entity.refundedQuantity + input.quantity))
            val product = productDao().getProductByIdOnce(input.productId) ?: continue
            val previousStock = product.stockQuantity
            val newStock = previousStock + input.quantity
            productDao().updateStock(input.productId, newStock)
            stockMovementDao().insertMovement(
                StockMovementEntity(
                    productId = input.productId,
                    productName = product.name,
                    movementType = "RETURN",
                    quantity = input.quantity,
                    previousStock = previousStock,
                    newStock = newStock,
                    reason = reason.ifBlank { "Return for sale #$saleId" },
                    createdAt = now,
                    createdBy = "Cashier",
                    syncStatus = "PENDING_CREATE"
                )
            )
        }

        val updatedItems = saleItemDao().getItemsBySaleId(saleId).first()
        val isFullReturn = updatedItems.all { it.refundedQuantity >= it.quantity }

        val jsonArray = org.json.JSONArray()
        items.forEach { input ->
            val obj = org.json.JSONObject()
            obj.put("productId", input.productId)
            obj.put("quantity", input.quantity)
            obj.put("refundAmount", input.refundAmount)
            jsonArray.put(obj)
        }

        val record = ReturnRecordEntity(
            saleId = saleId,
            refundAmount = totalRefund,
            reason = reason,
            isFullReturn = isFullReturn,
            returnedItemsJson = jsonArray.toString()
        )
        val recordId = returnRecordDao().insert(record)

        val sale = saleDao().getSaleById(saleId) ?: return recordId
        saleDao().updateSale(sale.copy(refundedAmount = sale.refundedAmount + totalRefund))
        return recordId
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `customer_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT, `email` TEXT, `address` TEXT, `total_purchases` REAL NOT NULL DEFAULT 0.0, `visit_count` INTEGER NOT NULL DEFAULT 0, `last_visit` INTEGER, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `closeout_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cashier_id` INTEGER, `cashier_name` TEXT NOT NULL DEFAULT '', `opening_balance` REAL NOT NULL DEFAULT 0.0, `closing_balance` REAL NOT NULL DEFAULT 0.0, `expected_cash` REAL NOT NULL DEFAULT 0.0, `actual_cash` REAL NOT NULL DEFAULT 0.0, `difference` REAL NOT NULL DEFAULT 0.0, `total_sales` REAL NOT NULL DEFAULT 0.0, `total_returns` REAL NOT NULL DEFAULT 0.0, `transaction_count` INTEGER NOT NULL DEFAULT 0, `notes` TEXT, `closed_at` INTEGER NOT NULL, `created_at` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `voided_sales` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sale_id` INTEGER NOT NULL, `original_transaction_number` TEXT NOT NULL DEFAULT '', `original_total` REAL NOT NULL DEFAULT 0.0, `reason` TEXT NOT NULL DEFAULT '', `voided_by` TEXT NOT NULL DEFAULT '', `voided_at` INTEGER NOT NULL)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN base_price REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sales ADD COLUMN payment_reference TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "portable_pos_database"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration(true)
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