package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Tasks : IntIdTable("tasks") {
    val title = varchar("title", 255)
    val description = varchar("description", 1_000).nullable()
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.CASCADE)
}

@Serializable
data class TaskDto(
    val id: Int,
    val title: String,
    val description: String?,
    val categoryId: Int,
)

@Serializable
data class TaskCreateRequest(
    val title: String,
    val description: String? = null,
    val categoryId: Int,
)

@Serializable
data class TaskUpdateRequest(
    val title: String,
    val description: String? = null,
    val categoryId: Int,
)
