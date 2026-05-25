package dev.ml.portablepos.data.repository

import dev.ml.portablepos.data.local.dao.CategoryDao
import dev.ml.portablepos.data.mapper.toDomainModel
import dev.ml.portablepos.data.mapper.toEntity
import dev.ml.portablepos.domain.model.Category
import dev.ml.portablepos.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)?.toDomainModel()

    override suspend fun insert(category: Category): Long =
        categoryDao.insert(category.toEntity())

    override suspend fun update(category: Category) =
        categoryDao.update(category.toEntity())

    override suspend fun delete(category: Category) =
        categoryDao.delete(category.toEntity())
}
