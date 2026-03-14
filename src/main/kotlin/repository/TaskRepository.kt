package com.example.repository

import com.example.database.DatabaseFactory
import com.example.models.Categories
import com.example.models.TaskCreateRequest
import com.example.models.TaskDto
import com.example.models.TaskUpdateRequest
import com.example.models.Tasks
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class TaskRepository {
    suspend fun getAll(categoryId: Int?): List<TaskDto> = DatabaseFactory.dbQuery {
        if (categoryId != null) {
            ensureCategoryExists(categoryId)
        }

        val query = if (categoryId != null) {
            Tasks.select { Tasks.categoryId eq DatabaseFactory.categoryEntityId(categoryId) }
        } else {
            Tasks.selectAll()
        }

        query.map(::toTaskDto)
    }

    suspend fun getById(id: Int): TaskDto = DatabaseFactory.dbQuery {
        Tasks.select { Tasks.id eq DatabaseFactory.taskEntityId(id) }
            .singleOrNull()
            ?.let(::toTaskDto)
            ?: throw NotFoundException("Task with id=$id was not found")
    }

    suspend fun create(request: TaskCreateRequest): TaskDto = DatabaseFactory.dbQuery {
        validateRequest(request.title, request.categoryId)

        val normalizedTitle = request.title.trim()
        val normalizedDescription = request.description?.trim()?.takeIf(String::isNotEmpty)

        try {
            val id = Tasks.insertAndGetId {
                it[title] = normalizedTitle
                it[description] = normalizedDescription
                it[categoryId] = DatabaseFactory.categoryEntityId(request.categoryId)
            }.value

            TaskDto(
                id = id,
                title = normalizedTitle,
                description = normalizedDescription,
                categoryId = request.categoryId,
            )
        } catch (exception: ExposedSQLException) {
            if (DatabaseFactory.isConstraintViolation(exception)) {
                throw ConflictException("Task references an invalid category or violates a database constraint")
            }
            throw exception
        }
    }

    suspend fun update(id: Int, request: TaskUpdateRequest): TaskDto = DatabaseFactory.dbQuery {
        validateRequest(request.title, request.categoryId)

        val normalizedTitle = request.title.trim()
        val normalizedDescription = request.description?.trim()?.takeIf(String::isNotEmpty)

        val updatedRows = try {
            Tasks.update({ Tasks.id eq DatabaseFactory.taskEntityId(id) }) {
                it[title] = normalizedTitle
                it[description] = normalizedDescription
                it[categoryId] = DatabaseFactory.categoryEntityId(request.categoryId)
            }
        } catch (exception: ExposedSQLException) {
            if (DatabaseFactory.isConstraintViolation(exception)) {
                throw ConflictException("Task references an invalid category or violates a database constraint")
            }
            throw exception
        }

        if (updatedRows == 0) {
            throw NotFoundException("Task with id=$id was not found")
        }

        TaskDto(
            id = id,
            title = normalizedTitle,
            description = normalizedDescription,
            categoryId = request.categoryId,
        )
    }

    suspend fun delete(id: Int) = DatabaseFactory.dbQuery {
        val deletedRows = Tasks.deleteWhere { Tasks.id eq DatabaseFactory.taskEntityId(id) }
        if (deletedRows == 0) {
            throw NotFoundException("Task with id=$id was not found")
        }
    }

    private fun validateRequest(title: String, categoryId: Int) {
        if (title.isBlank()) {
            throw ValidationException("Task title must not be blank")
        }
        ensureCategoryExists(categoryId)
    }

    private fun ensureCategoryExists(categoryId: Int) {
        val exists = Categories.select { Categories.id eq DatabaseFactory.categoryEntityId(categoryId) }.empty().not()
        if (!exists) {
            throw ValidationException("Category with id=$categoryId does not exist")
        }
    }

    private fun toTaskDto(row: ResultRow): TaskDto = TaskDto(
        id = row[Tasks.id].value,
        title = row[Tasks.title],
        description = row[Tasks.description],
        categoryId = row[Tasks.categoryId].value,
    )
}
