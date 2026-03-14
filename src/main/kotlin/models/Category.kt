package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

object Categories : IntIdTable("categories") {
    val name = varchar("name", 255).uniqueIndex()
}

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String,
)

@Serializable
data class CategoryCreateRequest(
    val name: String,
)

@Serializable
data class CategoryUpdateRequest(
    val name: String,
)
