package org.example
// service/AmpsService.kt
import com.crankuptheamps.client.Client
import com.crankuptheamps.client.Message
import com.crankuptheamps.client.MessageStream
import com.crankuptheamps.client.exception.AMPSException
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class AmpsService(private val config: AmpsConfig) : CoroutineVerticle() {
    private val running = AtomicBoolean(false)
    private lateinit var client: Client

    override suspend fun start() {
        running.set(true)
        client = Client(config.clientName)

        try {
            client.connect(config.uri)
            println("Connected to AMPS server at ${config.uri}")

            // Start consumer
            launchConsumer()

            // Start producer if needed
            launchProducer()

            // Call this in start()
           // setupEventBusHandlers()
        } catch (e: AMPSException) {
            println("Failed to connect to AMPS: ${e.message}")
            stop()
        }
    }

    override suspend fun stop() {
        running.set(false)
        try {
            client.close()
            println("AMPS client disconnected")
        } catch (e: Exception) {
            println("Error disconnecting from AMPS: ${e.message}")
        }
    }

    private fun launchConsumer() = vertx.launch {
        while (running.get()) {
            try {
                val messageStream: MessageStream = client.subscribe(config.topic)
                println("Subscribed to topic ${config.topic}")

                while (running.get()) {
                    val message = messageStream.next()
                    processMessage(message)
                }
            } catch (e: AMPSException) {
                println("AMPS consumer error: ${e.message}. Reconnecting...")
                delay(5000)
            }
        }
    }

    private fun launchProducer() = vertx.launch {
        var counter = 0
        while (running.get()) {
            try {
                val message = Message(config.topic).apply {
                    data = JsonObject()
                        .put("id", counter)
                        .put("message", "Hello from Vert.x!")
                        .put("timestamp", System.currentTimeMillis())
                        .toString()
                }

                client.publish(message)
                counter++
                delay(config.messageInterval)
            } catch (e: AMPSException) {
                println("AMPS producer error: ${e.message}")
                delay(5000)
            }
        }
    }

    private fun processMessage(message: Message) {
        println("""
            Received AMPS message:
            Topic: ${message.topic}
            Data: ${message.data}
            Bookmark: ${message.bookmark}
            SOW key: ${message.sowKey}
        """.trimIndent())

        // Process your message here
        // You can publish to Vert.x event bus if needed
        vertx.eventBus().publish("amps.messages", message.data)
    }

    fun publishToAmps(data: String): Boolean {
        return try {
            val message = Message(config.topic).apply {
                this.data = data
            }
            client.publish(message)
            true
        } catch (e: Exception) {
            println("Error publishing to AMPS: ${e.message}")
            false
        }
    }

    // Add to AmpsService.kt
   /* private fun setupEventBusHandlers() {
        vertx.eventBus().consumer<JsonObject>("amps.publish") { message ->
            val success = publishToAmps(message.body().toString())
            message.reply(success)
        }

        vertx.eventBus().consumer<String>("amps.command") { message ->
            when (message.body()) {
                "status" -> message.reply("""{"connected": ${client.isConnected()}}""")
                "stop" -> {
                    stop()
                    message.reply("""{"status": "stopped"}""")
                }
                else -> message.fail(400, "Unknown command")
            }
        }
    }*/

}