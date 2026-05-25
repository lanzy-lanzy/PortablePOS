package dev.ml.portablepos.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.ml.portablepos.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT c.id, c.firebase_id AS firebaseId, c.name, c.description, c.sync_status AS syncStatus, c.last_synced_at AS lastSyncedAt, c.created_at AS createdAt, c.updated_at AS updatedAt, (SELECT COUNT(*) FROM products p WHERE p.category_id = c.id) AS productCount FROM categories c ORDER BY c.name ASC")
    fun getCategoryWithProductCount(): Flow<List<CategoryWithProductCount>>
}

data class CategoryWithProductCount(
    val id: Long,
    val firebaseId: String?,
    val name: String,
    val description: String?,
    val syncStatus: String,
    val lastSyncedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val productCount: Int
)
