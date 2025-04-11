package org.example
// Main.kt
import org.example.AmpsConfig
import org.example.AppConfig
import org.example.loadAppConfig
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import org.example.AmpsService
import org.example.AmpsApi

suspend fun main() {
    val vertx = Vertx.vertx()

    try {
        // Load configuration
        val appConfig = vertx.loadAppConfig()
        println("Loaded configuration: $appConfig")

        // Deploy AMPS service
        val ampsService = AmpsService(appConfig.amps)
        vertx.deployVerticle(ampsService).await()

        // Setup HTTP server
        val router = io.vertx.ext.web.Router.router(vertx)

        // Setup AMPS API
        AmpsApi(ampsService).setupRouter(router)

        // Add basic route
        router.get("/").handler { ctx ->
            ctx.response()
                .putHeader("content-type", "text/plain")
                .end("Vert.x AMPS Stream Example")
        }

        // Start HTTP server
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(appConfig.httpPort)
            .await()

        println("Application started on port ${appConfig.httpPort}")
        println("AMPS connected to ${appConfig.amps.uri}, topic: ${appConfig.amps.topic}")

        // Keep the application running
        readLine()
    } finally {
        vertx.close().await()
    }
}