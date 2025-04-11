package org.example

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import io.vertx.config.ConfigRetriever

suspend fun Vertx.loadAppConfig(): AppConfig {
    val configRetriever = ConfigRetriever.create(
        this,
        io.vertx.config.ConfigRetrieverOptions()
            .addStore(
                io.vertx.config.ConfigStoreOptions()
                    .setType("file")
                    .setConfig(JsonObject().put("path", "config.json"))
            )
    )

    val config = configRetriever.config.await()

    return AppConfig(
        httpPort = config.getInteger("httpPort", 8080),
        amps = AmpsConfig(
            uri = config.getJsonObject("amps").getString("uri"),
            topic = config.getJsonObject("amps").getString("topic"),
            clientName = config.getJsonObject("amps").getString("clientName"),
            messageInterval = config.getJsonObject("amps").getLong("messageInterval", 2000)
        )
    )
}