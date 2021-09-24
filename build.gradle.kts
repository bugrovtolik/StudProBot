plugins {
    application
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.abuhrov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.telegram:telegrambots:5.3.0")
    implementation("redis.clients:jedis:3.6.3")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20210629-1.32.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.1.0")
}

tasks {
    withType<Jar> {
        manifest { attributes(mapOf("Main-Class" to "MainKt")) }
    }
    register("stage") {
        dependsOn("build")
    }
}
