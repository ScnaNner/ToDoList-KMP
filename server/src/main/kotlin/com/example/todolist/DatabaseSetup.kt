package com.example.todolist

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

// SQL Table
object TasksTable : Table() {
    val id = integer("id").autoIncrement() // The DB will handle IDs automatically
    val title = varchar("title", 128)
    val isCompleted = bool("isCompleted").default(false)

    override val primaryKey = PrimaryKey(id)
}

// Connect to SQLite and create the table if it doesn't exist
fun initDatabase() {
    // This creates a file called "tasks.db" in your project folder
    Database.connect("jdbc:sqlite:tasks.db", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(TasksTable)
    }
}