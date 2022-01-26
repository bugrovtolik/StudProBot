plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.6.10"
}

group = "com.abuhrov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.telegram:telegrambots:5.6.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20210629-1.32.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.2.2")
    implementation("org.hibernate:hibernate-core:5.6.3.Final")
    implementation("org.apache.logging.log4j:log4j-core:2.17.0")
    implementation("org.slf4j:slf4j-simple:1.7.33")
    runtimeOnly("org.postgresql:postgresql:42.3.1")
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
