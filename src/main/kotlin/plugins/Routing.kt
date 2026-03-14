package com.example.plugins

import com.example.routes.categoryRoutes
import com.example.routes.taskRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Ktor tasks API is running")
        }

        categoryRoutes()
        taskRoutes()
    }
}
