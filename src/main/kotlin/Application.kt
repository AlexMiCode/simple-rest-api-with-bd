package com.example

import com.example.database.DatabaseFactory
import com.example.plugins.configureErrorHandling
import com.example.plugins.configureLogging
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    configureSerialization()
    configureLogging()
    configureErrorHandling()
    configureRouting()
}
