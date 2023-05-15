plugins {
    kotlin("js") version "1.8.20"
}

group = "com.gitlab.hurtling"
version = "0.1"

repositories {
    mavenCentral()
}

kotlin {
    js {
        binaries.executable()
        nodejs {

        }
    }
}