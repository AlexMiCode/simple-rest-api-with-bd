package com.example.routes

import com.example.models.CategoryCreateRequest
import com.example.models.CategoryUpdateRequest
import com.example.repository.CategoryRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.categoryRoutes() {
    val repository = CategoryRepository()

    route("/api/categories") {
        get {
            call.respond(repository.getAll())
        }

        get("/{id}") {
            call.respond(repository.getById(call.categoryId()))
        }

        get("/{id}/tasks") {
            call.respond(repository.getTasks(call.categoryId()))
        }

        post {
            val request = call.receive<CategoryCreateRequest>()
            call.respond(HttpStatusCode.Created, repository.create(request))
        }

        put("/{id}") {
            val request = call.receive<CategoryUpdateRequest>()
            call.respond(repository.update(call.categoryId(), request))
        }

        delete("/{id}") {
            repository.delete(call.categoryId())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
