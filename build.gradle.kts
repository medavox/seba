import java.io.IOException

plugins {
    kotlin("js") version "1.8.20"
}

group = "com.gitlab.hurtling"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.2")
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

task("convertTextFilesToStrings") {
    println(projectDir.absolutePath)
    val resDir = "src/main/resources"
    val cubeBlocksDir = "$resDir/CubeBlocks"
    val cubeBlocksListing = File(cubeBlocksDir).listFiles()

    val outputDir = File(rootDir, "src/main/kotlin/generated")
    if( !outputDir.mkdirs() && !outputDir.isDirectory) {
        throw IOException("couldn't create output dir for sbc files")
    }

    fun File.writeOut() {
        val className = this.nameWithoutExtension.replace(".", "")
        val fileName = className+".kt"
        val outputFile = File(outputDir, fileName)
        outputFile.writeText("object $className {\n\t")
        val content = resources.text.fromFile(this).asReader().readText()
        outputFile.appendText("val content = \"\"\"$content\"\"\"\n}")
    }

    File(resDir, "Components.sbc").writeOut()
    File(resDir, "Localization/MyTexts.resx").writeOut()
    for( file in cubeBlocksListing) {
        file.writeOut()
    }
}
