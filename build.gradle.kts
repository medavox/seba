import java.io.IOException

plugins {
    kotlin("js") version "1.8.20"
}

group = "com.github.medavox"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
}
