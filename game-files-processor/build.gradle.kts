import java.io.IOException

plugins {
    kotlin("jvm") version "1.8.20"
    application
}

group = "com.gitlab.hurtling"
version = "0.1"

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jsoup:jsoup:1.16.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}

