package com.example.todolist

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.respond
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod

fun main() {
    // Start the Netty engine on port 8080/8081
    embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {

    install(CORS) {
        anyHost() // For local development only!
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
    }

    // 🛡️ THE SAFETY NET: Catch all crashes globally
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            // 1. Print the red text to your AndroidStudio console so YOU can fix it
            cause.printStackTrace()

            // 2. Send a polite 500 Error to the phone so the app doesn't freeze waiting
            call.respond(HttpStatusCode.InternalServerError, "Server Error: ${cause.localizedMessage}")
        }
    }

    install(ContentNegotiation) {
        json()
    }

    initDatabase()
    configureTaskRoutes()
}