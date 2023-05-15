import java.io.IOException

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
task("convertTextFilesToStrings") {
    println(projectDir.absolutePath)
    val resDir = "src/main/resources"
    val cubeBlocksDir = "$resDir/CubeBlocks"
    val cubeBlocksListing = File(cubeBlocksDir).listFiles()

    val outputDir = File(rootDir, "src/main/kotlin/generated")
    if( !outputDir.mkdirs() && !outputDir.isDirectory) {
        throw IOException("couldn't create output dir for sbc files")
    }

    fun File.writeOut(): String {
        if(!this.exists()) {
            throw IOException("file not found: ${this.absolutePath}")
        }
        val className = this.nameWithoutExtension.replace(".", "")
        val fileName = "$className.kt"
        val outputFile = File(outputDir, fileName)
        val content = resources.text.fromFile(this).asReader().readText()
        outputFile.writeText("package generated\nval $className = \"\"\"$content\"\"\"\n")
        return className
    }

    val cubeBlocksList = File(outputDir, "CubeBlocksList.kt")
    File(resDir, "Components.sbc").writeOut()
    File(resDir, "Localization/MyTexts.resx").writeOut()
    cubeBlocksList.writeText("package generated\nval cubeBlocksList = listOf(Components, MyTexts, ")
    for( file in cubeBlocksListing) {
        cubeBlocksList.appendText(" ${file.writeOut()},")
    }
    cubeBlocksList.appendText(")")
}
