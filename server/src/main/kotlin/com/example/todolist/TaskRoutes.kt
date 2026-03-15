package com.example.todolist

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere


// Helper function to translate a Database Row into your Shared Task object
fun ResultRow.toTask(): Task {
    return Task(
        id = this[TasksTable.id],
        title = this[TasksTable.title],
        isCompleted = this[TasksTable.isCompleted]
    )
}

fun Application.configureTaskRoutes() {
    routing {
        route("/tasks") {

            // 1. GET TASKS FROM DB (With optional filtering)
            get {
                // Look for "?status=" in the URL
                val statusQuery = call.request.queryParameters["status"]

                val tasks = transaction {
                    when (statusQuery) {
                        "completed" -> TasksTable.select { TasksTable.isCompleted eq true }
                        "pending" -> TasksTable.select { TasksTable.isCompleted eq false }
                        else -> TasksTable.selectAll() // If no query, return everything
                    }.map { it.toTask() }
                }

                call.respond(tasks)
            }

            // 2. INSERT NEW TASK INTO DB
            post {
                try {
                    val incomingTask = call.receive<Task>()

                    // 🚨 THE BOUNCER: Check the data before touching the database!
                    if (incomingTask.title.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Task title cannot be empty!")
                        return@post // Stop here, do not save!
                    }

                    val newTaskId = transaction {
                        // Insert the data and grab the auto-generated ID from SQLite
                        TasksTable.insert {
                            it[title] = incomingTask.title
                            it[isCompleted] = incomingTask.isCompleted
                        } get TasksTable.id
                    }

                    // Respond with the newly saved task (including its real DB id)
                    val savedTask = incomingTask.copy(id = newTaskId)
                    call.respond(HttpStatusCode.Created, savedTask)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
                }
            }

            // 3. GET ONE TASK BY ID FROM DB
            get("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val task = transaction {
                    TasksTable.select { TasksTable.id eq id }
                        .map { it.toTask() }
                        .singleOrNull()
                }

                if (task != null) {
                    call.respond(task)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            // 4. DELETE A TASK FROM DB
            delete("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@delete
                }

                // Delete the row from the database
                val deletedCount = transaction {
                    // Exposed uses .deleteWhere to remove rows matching a condition
                    TasksTable.deleteWhere { TasksTable.id eq id }
                }

                if (deletedCount > 0) {
                    call.respond(HttpStatusCode.OK, "Task deleted successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Task not found")
                }
            }

            // 5. UPDATE A TASK IN DB
            // A specific endpoint just for toggling! No JSON body needed.
            put("{id}/toggle") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }

                transaction {
                    // 1. Find the current task in the database
                    val currentTask = TasksTable.select { TasksTable.id eq id }.singleOrNull()

                    if (currentTask != null) {
                        val currentStatus = currentTask[TasksTable.isCompleted]

                        // 2. Update just that one row to be the opposite of what it was
                        TasksTable.update({ TasksTable.id eq id }) {
                            it[isCompleted] = !currentStatus
                        }
                    }
                }

                call.respond(HttpStatusCode.OK, "Task toggled successfully")
            }

            // 5. UPDATE A TASK IN DB
            put("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }

                try {
                    // Read the incoming updated task
                    val updatedTask = call.receive<Task>()

                    // Update the database
                    transaction {
                        // Exposed uses .update to modify existing rows
                        TasksTable.update({ TasksTable.id eq id }) {
                            it[title] = updatedTask.title
                            it[isCompleted] = updatedTask.isCompleted
                        }
                    }
                    call.respond(HttpStatusCode.OK, "Task updated successfully")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid data format")
                }
            }
        }
    }
}