package org.example

// api/AmpsApi.kt
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import org.example.AmpsService
// Add to AmpsApi.kt
//import io.vertx.ext.web.handler.sockjs.SockJSHandler

class AmpsApi(private val ampsService: AmpsService) {
    fun setupRouter(router: Router) {
        router.get("/api/amps/messages").coroutineHandler { ctx -> getMessages(ctx) }
        router.post("/api/amps/messages").coroutineHandler { ctx -> postMessage(ctx) }
    }

    private suspend fun getMessages(ctx: RoutingContext) {
        // In a real app, you might get messages from a buffer or database
        ctx.response()
            .putHeader("content-type", "application/json")
            .end("""{"status": "Use WebSocket or SSE for real-time messages"}""")
            .await()
    }

    private suspend fun postMessage(ctx: RoutingContext) {
        val body = ctx.body().asJsonObject()
        val success = ampsService.publishToAmps(body.toString())

        ctx.response()
            .putHeader("content-type", "application/json")
            .end(
                if (success) {
                    """{"status": "Message published to AMPS"}"""
                } else {
                    """{"status": "Failed to publish message"}"""
                }
            )
            .await()
    }
}

// Extension for coroutine handlers
fun Router.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
    this.handler { ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                fn(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
}