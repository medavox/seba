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
