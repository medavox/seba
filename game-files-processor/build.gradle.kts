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