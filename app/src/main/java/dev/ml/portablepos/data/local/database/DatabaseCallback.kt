package dev.ml.portablepos.data.local.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val now = System.currentTimeMillis()

        db.execSQL(
            """
            INSERT INTO categories (name, sync_status, created_at, updated_at)
            VALUES ('Beverages', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO categories (name, sync_status, created_at, updated_at)
            VALUES ('Snacks', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO categories (name, sync_status, created_at, updated_at)
            VALUES ('Grocery', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO categories (name, sync_status, created_at, updated_at)
            VALUES ('Personal Care', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO categories (name, sync_status, created_at, updated_at)
            VALUES ('Motorparts', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO categories (name, sync_status, created_at, updated_at)
            VALUES ('Medicine', 'SYNCED', $now, $now)
            """
        )

        db.execSQL(
            """
            INSERT INTO products (name, barcode, category_id, cost_price, selling_price, stock_quantity, reorder_level, unit, sync_status, created_at, updated_at)
            VALUES ('Coca-Cola 1L', '4901234567890', 1, 45.0, 55.0, 50, 10, 'bottle', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO products (name, barcode, category_id, cost_price, selling_price, stock_quantity, reorder_level, unit, sync_status, created_at, updated_at)
            VALUES ('Piattos', '4801234567890', 2, 20.0, 30.0, 100, 20, 'pcs', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO products (name, barcode, category_id, cost_price, selling_price, stock_quantity, reorder_level, unit, sync_status, created_at, updated_at)
            VALUES ('Sardines', '4801234567891', 3, 15.0, 22.0, 75, 15, 'pcs', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO products (name, barcode, category_id, cost_price, selling_price, stock_quantity, reorder_level, unit, sync_status, created_at, updated_at)
            VALUES ('Shampoo sachet', '4801234567892', 4, 5.0, 8.0, 200, 50, 'pcs', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO products (name, barcode, category_id, cost_price, selling_price, stock_quantity, reorder_level, unit, sync_status, created_at, updated_at)
            VALUES ('Motor Oil 1L', '4801234567893', 5, 120.0, 180.0, 30, 5, 'bottle', 'SYNCED', $now, $now)
            """
        )
        db.execSQL(
            """
            INSERT INTO products (name, barcode, category_id, cost_price, selling_price, stock_quantity, reorder_level, unit, sync_status, created_at, updated_at)
            VALUES ('Biogesic', '4801234567894', 6, 8.0, 12.0, 150, 30, 'pcs', 'SYNCED', $now, $now)
            """
        )

        db.execSQL(
            """
            INSERT INTO cashiers (full_name, username, role, pin_code, sync_status, created_at)
            VALUES ('Admin', 'admin', 'Admin', '1234', 'SYNCED', $now)
            """
        )
    }
}