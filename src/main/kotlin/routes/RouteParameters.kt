package com.example.routes

import com.example.repository.ValidationException
import io.ktor.server.application.ApplicationCall

fun ApplicationCall.categoryId(): Int = parameters["id"]?.toIntOrNull()
    ?: throw ValidationException("Category id must be an integer")

fun ApplicationCall.taskId(): Int = parameters["id"]?.toIntOrNull()
    ?: throw ValidationException("Task id must be an integer")
