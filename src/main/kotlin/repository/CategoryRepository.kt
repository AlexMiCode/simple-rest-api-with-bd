package com.example.repository

import com.example.database.DatabaseFactory
import com.example.models.Categories
import com.example.models.CategoryCreateRequest
import com.example.models.CategoryDto
import com.example.models.CategoryUpdateRequest
import com.example.models.TaskDto
import com.example.models.Tasks
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class CategoryRepository {
    suspend fun getAll(): List<CategoryDto> = DatabaseFactory.dbQuery {
        Categories.selectAll().map(::toCategoryDto)
    }

    suspend fun getById(id: Int): CategoryDto = DatabaseFactory.dbQuery {
        Categories.select { Categories.id eq DatabaseFactory.categoryEntityId(id) }
            .singleOrNull()
            ?.let(::toCategoryDto)
            ?: throw NotFoundException("Category with id=$id was not found")
    }

    suspend fun create(request: CategoryCreateRequest): CategoryDto = DatabaseFactory.dbQuery {
        validateName(request.name)

        try {
            val normalizedName = request.name.trim()
            val id = Categories.insertAndGetId {
                it[name] = normalizedName
            }.value
            CategoryDto(id = id, name = normalizedName)
        } catch (exception: ExposedSQLException) {
            if (DatabaseFactory.isConstraintViolation(exception)) {
                throw ConflictException("Category with name='${request.name.trim()}' already exists")
            }
            throw exception
        }
    }

    suspend fun update(id: Int, request: CategoryUpdateRequest): CategoryDto = DatabaseFactory.dbQuery {
        validateName(request.name)

        val normalizedName = request.name.trim()
        val updatedRows = try {
            Categories.update({ Categories.id eq DatabaseFactory.categoryEntityId(id) }) {
                it[name] = normalizedName
            }
        } catch (exception: ExposedSQLException) {
            if (DatabaseFactory.isConstraintViolation(exception)) {
                throw ConflictException("Category with name='${normalizedName}' already exists")
            }
            throw exception
        }

        if (updatedRows == 0) {
            throw NotFoundException("Category with id=$id was not found")
        }

        CategoryDto(id = id, name = normalizedName)
    }

    suspend fun delete(id: Int) = DatabaseFactory.dbQuery {
        val deletedRows = Categories.deleteWhere { Categories.id eq DatabaseFactory.categoryEntityId(id) }
        if (deletedRows == 0) {
            throw NotFoundException("Category with id=$id was not found")
        }
    }

    suspend fun getTasks(categoryId: Int): List<TaskDto> = DatabaseFactory.dbQuery {
        ensureCategoryExists(categoryId)
        Tasks.select { Tasks.categoryId eq DatabaseFactory.categoryEntityId(categoryId) }
            .map(::toTaskDto)
    }

    private fun ensureCategoryExists(id: Int) {
        val exists = Categories.select { Categories.id eq DatabaseFactory.categoryEntityId(id) }.empty().not()
        if (!exists) {
            throw NotFoundException("Category with id=$id was not found")
        }
    }

    private fun validateName(name: String) {
        if (name.isBlank()) {
            throw ValidationException("Category name must not be blank")
        }
    }

    private fun toCategoryDto(row: ResultRow): CategoryDto = CategoryDto(
        id = row[Categories.id].value,
        name = row[Categories.name],
    )

    private fun toTaskDto(row: ResultRow): TaskDto = TaskDto(
        id = row[Tasks.id].value,
        title = row[Tasks.title],
        description = row[Tasks.description],
        categoryId = row[Tasks.categoryId].value,
    )
}
