plugins {
    application
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.serialization") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.abuhrov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    implementation("org.telegram:telegrambots:5.1.0")
    implementation("redis.clients:jedis:3.5.1")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20200922-1.30.10")
    implementation("com.google.auth:google-auth-library-oauth2-http:0.21.1")
}

application {
    mainClassName = "MainKt"
}

tasks {
    withType<Jar> {
        manifest { attributes(mapOf("Main-Class" to application.mainClassName)) }
    }
    register("stage") {
        dependsOn("build")
    }
}
