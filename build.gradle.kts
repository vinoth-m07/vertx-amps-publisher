plugins {
    kotlin("jvm") version "2.1.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Vert.x core
    implementation("io.vertx:vertx-core:4.4.5")
    // Vert.x Kotlin support
    implementation("io.vertx:vertx-lang-kotlin:4.4.5")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.4.5")
    implementation("io.vertx:vertx-web:4.5.14")
    // AMPS client (60East's AMPS)
    //implementation("com.crankuptheamps:amps-client:5.3.3.0")
// https://mvnrepository.com/artifact/com.crankuptheamps/amps-client
    implementation("com.crankuptheamps:amps-client:5.3.3.0")
    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.7")
    // Config
    implementation("io.vertx:vertx-config:4.4.5")

    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

application {
    mainClass.set("MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
    kotlinOptions.freeCompilerArgs += listOf("-Xjsr305=strict")

}