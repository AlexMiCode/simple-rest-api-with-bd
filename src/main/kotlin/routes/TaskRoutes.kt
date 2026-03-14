package com.example.routes

import com.example.models.TaskCreateRequest
import com.example.models.TaskUpdateRequest
import com.example.repository.TaskRepository
import com.example.repository.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.taskRoutes() {
    val repository = TaskRepository()

    route("/api/tasks") {
        get {
            val categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull()
                ?: call.request.queryParameters["categoryId"]?.let {
                    throw ValidationException("categoryId must be an integer")
                }

            call.respond(repository.getAll(categoryId))
        }

        get("/{id}") {
            call.respond(repository.getById(call.taskId()))
        }

        post {
            val request = call.receive<TaskCreateRequest>()
            call.respond(HttpStatusCode.Created, repository.create(request))
        }

        put("/{id}") {
            val request = call.receive<TaskUpdateRequest>()
            call.respond(repository.update(call.taskId(), request))
        }

        delete("/{id}") {
            repository.delete(call.taskId())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
