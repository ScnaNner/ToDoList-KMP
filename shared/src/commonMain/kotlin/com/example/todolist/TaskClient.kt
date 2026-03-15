package com.example.todolist

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object TaskApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // Use 10.0.2.2 for Android Emulator
    //private const val BASE_URL = "http://10.0.2.2:8081/tasks"
    //Web, desktop and IOS emulator testing
    const val BASE_URL = "http://localhost:8081/tasks"
    //IOS testing (External device)
    //const val BASE_URL = "http://172.20.10.X:8081/tasks"

    suspend fun getAllTasks(status: String? = null): List<Task> {
        // If a status is provided, attach it. Otherwise, just use the normal URL.
        val url = if (status != null) "$BASE_URL?status=$status" else BASE_URL
        return client.get(url).body()
    }

    suspend fun addTask(task: Task) {
        client.post(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody(task)
        }
    }

    suspend fun deleteTask(id: Int) {
        client.delete("$BASE_URL/$id")
    }

    // We just tell the server WHICH ID to flip!
    suspend fun toggleTask(id: Int) {
        // Hits the new endpoint: http://10.0.2.2:8081/tasks/5/toggle
        client.put("$BASE_URL/$id/toggle")
    }

    // Update an existing task
    suspend fun updateTask(task: Task) {
        client.put("$BASE_URL/${task.id}") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(task)
        }
    }
}
