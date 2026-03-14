package com.example.database

import com.example.models.Categories
import com.example.models.Tasks
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.Dispatchers
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val hikariConfig = HikariConfig().apply {
            driverClassName = config.property("database.driver").getString()
            jdbcUrl = config.property("database.url").getString()
            username = config.property("database.user").getString()
            password = config.property("database.password").getString()
            maximumPoolSize = config.property("database.maximumPoolSize").getString().toInt()
            isAutoCommit = false
            validate()
        }

        Database.connect(HikariDataSource(hikariConfig))

        transaction {
            SchemaUtils.create(Categories, Tasks)
            seedIfNeeded()
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    fun categoryEntityId(id: Int): EntityID<Int> = EntityID(id, Categories)

    fun taskEntityId(id: Int): EntityID<Int> = EntityID(id, Tasks)

    fun isConstraintViolation(cause: Throwable): Boolean {
        val exposedCause = cause as? ExposedSQLException
        val sqlState = exposedCause?.sqlState
        return sqlState == "23505" || sqlState == "23503" || cause.cause is JdbcSQLIntegrityConstraintViolationException
    }

    private fun seedIfNeeded() {
        if (!Categories.selectAll().empty()) {
            return
        }

        val workId = Categories.insertAndGetId { it[name] = "Work" }
        val studyId = Categories.insertAndGetId { it[name] = "Study" }
        val homeId = Categories.insertAndGetId { it[name] = "Home" }

        val seedTasks = listOf(
            Triple("Finish quarterly report", "Finalize metrics and send the PDF to management.", workId),
            Triple("Prepare sprint demo", "Collect screenshots and record a short walkthrough.", workId),
            Triple("Review pull requests", "Check backend changes before merge.", workId),
            Triple("Read Ktor documentation", "Focus on routing and plugins.", studyId),
            Triple("Practice SQL joins", "Solve five one-to-many query examples.", studyId),
            Triple("Update study notes", "Summarize Exposed transaction rules.", studyId),
            Triple("Buy groceries", "Milk, eggs, vegetables, and bread.", homeId),
            Triple("Clean kitchen", "Wipe surfaces and sort pantry shelves.", homeId),
            Triple("Pay utility bills", "Electricity and internet before weekend.", homeId)
        )

        seedTasks.forEach { (title, description, categoryId) ->
            Tasks.insert {
                it[Tasks.title] = title
                it[Tasks.description] = description
                it[Tasks.categoryId] = categoryId
            }
        }
    }
}
