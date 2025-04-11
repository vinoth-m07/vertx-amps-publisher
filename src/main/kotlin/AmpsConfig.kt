package org.example

data class AmpsConfig(
    val uri: String,
    val topic: String,
    val clientName: String,
    val messageInterval: Long = 2000
)
