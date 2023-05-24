import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

private val resDonor = object{}
val allBlockData: MutableList<BlockData> = mutableListOf()
val localisationStrings = mutableMapOf<String, String>()
private val powerTags = mutableSetOf<String>()
val components = mutableMapOf<String, Double>()

var identical = 0//number of components where the xsiType is the same as the typId
var empty = 0//number of components where the xsiType is empty
//TODO: railgun seems to be missing its recharge power data.
// possible leads: WeaponDefinitionId, ResourceSinkGroup, InventoryFillFactorMin

val idleOrMin = listOf(
    "PowerConsumptionIdle",
    "IdlePowerConsumption",
    "PowerInputIdle",
    "StandbyPowerConsumption",
    "RequiredIdlePowerInput",
    "MinSafeZonePowerDrainkW",
    "BasePowerInput",
    "MinPowerConsumption",
)
val activeOrMax = listOf(
    "ConsumptionPower",
    "RequiredPowerInput",
    "PowerInput",
    "MaxBroadcastPowerDrainkw",
    "PowerConsumptionMoving",
    "OperationalPowerConsumption",
    "PowerInputLasing",
    "PowerInputTurning",
    "MaxSafeZonePowerDrainkW",
    "MaxPowerConsumption",
)

/**Some blocks don't correctly list PCU data. This function provides workarounds.
 * Blocks in CubeBlocks_Armor_2 are missing their PCU entry.
 *  But in the game it's always 1.
 * ladders are uniquely missing their PCU in the XML.
 * I've manually checked their values in the game, and it's 1 for all of them.*/
private fun Element.getPcuWithFallback(file: String, typeSubtypeHuman: String): Int? {

    return when(typeSubtypeHuman) {
        "Ladder2/LadderShaft/Ladder Shaft" -> 1
        "Ladder2//Ladder" -> 1
        "Ladder2/LadderSmall/Ladder" -> 1
        else -> {
            this.getElementsByTag("PCU").firstOrNull()?.ownText()?.toInt()
                ?: if(file == "CubeBlocks_Armor_2.sbc") 1 else null
        }
    }
}

private fun readResource(path: String): String =
    resDonor.javaClass.getResource(path)?.readText() ?: throw FileNotFoundException("file not found: $path")

private fun initComponents() {
    val doc = Jsoup.parse(readResource("Components.sbc")).root()
    val entries = doc.getElementsByTag("Component")
    println("component entries: ${entries.size}")
    for(entry in entries) {
        val name = entry.getElementsByTag("SubtypeId")
            .firstOrNull()?.ownText()?: throw Exception("couldn't find component subtypeId")
        val mass = entry.getElementsByTag("Mass")
            .firstOrNull()?.ownText()?.toDouble()?: throw Exception("couldn't get mass for $name")
        components[name] = mass
    }
}

private fun initLocalisation() {
    val resDir = File(".", "game-files-processor/src/main/resources")
    val localisationFile:String = resDir.list { _, name: String ->
        name.matches(Regex("^MyTexts.*\\.resx$"))
    }?.first() ?: throw FileNotFoundException("couldn't find a MyTexts*.resx file in ${resDir.absolutePath}")
    val doc = Jsoup.parse(readResource(localisationFile)).root()
    val entries = doc.getElementsByTag("data")
    println("i18n entries: ${entries.size}")
    for(entry in entries) {
        val key = entry.attr("name")
        val value = entry.getElementsByTag("value").firstOrNull()?.ownText()
        if(value == null) {
            println("WARNING value for $key is missing")
            continue
        }
        localisationStrings[key] = value
    }
}

private fun Map<String, Int>.calculateMass(): Double = entries.fold(0.0) { acc, (name, count) ->
    acc + (count * (components[name] ?: 0.0) )
}

private fun initCubeBlockDefinitions() {
    val cubeBlocksDir = File(".", "game-files-processor/src/main/resources/CubeBlocks")
    val cubeBlocksFiles: List<File> = cubeBlocksDir.listFiles()?.toList() ?:
        throw IOException("couldn't list contents of "+cubeBlocksDir.absolutePath)
    println(File(".").absolutePath)
    val cubeBlocksXmlDocs: Map<String, Element> = cubeBlocksFiles.associateTo(mutableMapOf()) {
        it.name to Jsoup.parse(readResource("CubeBlocks/${it.name}"))
    }
        /*val cubeBlocksXmlDocs: Map<String, Element> = cubeBlocksList.mapValues {
            Jsoup.parse(it.value).root()
        }*/
    for((fileName, definitionsFile) in cubeBlocksXmlDocs) {
        val blockDefs: Element = definitionsFile.select("Definitions>CubeBlocks")
            .firstOrNull()?: throw Exception ("Block defs not found in $fileName")
        //println("${blockDefs.children().size} block defs in $file")
        for (block in blockDefs.children()) {
            block.children().toList().filter {
                it.tagName().lowercase().contains("power")
            }.forEach {
                powerTags.add(it.tagName())
            }
            //I thought I could uniquely identify all blocks by just their subtypeid, but no
            val subtypeId = block.getElementsByTag("SubtypeId").firstOrNull()?.ownText()
                ?: throw Exception("couldn't find subtype id in $fileName")
            val blockSize = block.getElementsByTag("CubeSize").firstOrNull()?.ownText()
                ?: throw Exception("couldn't find block size for $subtypeId")
            if(blockSize != "Large" && blockSize != "Small") throw Exception("block size for '$subtypeId' was neither Large nor Small: $blockSize")
            val typeId = block.getElementsByTag("TypeId")
                .firstOrNull()?: throw Exception("couldn't find type id in $fileName")
            val displayName = block.getElementsByTag("DisplayName").firstOrNull()
            if(displayName == null) {
                println("WARNING: couldn't find DisplayName for '$subtypeId' in $fileName")
                continue
            }
            val humanName: String = localisationStrings[displayName.ownText()] ?: subtypeId
            val xsiTypeSub = "${typeId.ownText()}/$subtypeId/$humanName"
            val pcu: Int? = block.getPcuWithFallback (fileName, xsiTypeSub)
            if(pcu == null) {
                println("WARNING: couldn't find PCU for '${typeId.ownText()}/$subtypeId/$humanName', $blockSize block in $fileName")
                continue
            }

            if(typeId.ownText().contains("_")) {
                println("weird typeId for subType '$subtypeId': ${typeId.ownText()}")
            }

            val componentsRaw = block.getElementsByTag("Components").firstOrNull()?.children()?:
                throw Exception("couldn't find Components for '${typeId.ownText()}/$subtypeId/$humanName' in $fileName")

            val components = CountingMap<String>()
            componentsRaw.forEach { component ->
                components[component.attr("Subtype")] += component.attr("Count").toInt()
            }

            val xsiType = block.attr("xsi:type")
                .replace("Definition", "")
                .replace("MyObjectBuilder_", "")

            val powerStorage:Int = block.getElementsByTag("MaxStoredPower")
                .firstOrNull()?.ownText()?.toDouble()?.times(1000)?.toInt() ?: 0
            val powerOutput:Int = block.getElementsByTag("MaxPowerOutput")
                .firstOrNull()?.ownText()?.toDouble()?.times(1000)?.toInt() ?: 0

            if(xsiType == typeId.ownText()) {
                identical++
            } else if(xsiType.isEmpty()) {
                empty++
            } else {
                //println("xsiType for $humanName is $xsiType")
            }

            allBlockData.add(BlockData(
                typeId = typeId.ownText().replace("MyObjectBuilder_", ""),
                subtypeId = subtypeId,
                pcu = pcu,
                humanName = humanName,
                size = if(blockSize == "Large") 'L' else 'S',
                components = components,
                mass = components.calculateMass(),
                xsiType = xsiType,
                maxPowerOutputKw = powerOutput,
                powerStorageKw = powerStorage,
            ))
        }
    }
}

private fun writeItAllOut() {
    val countingMapFileName = "CountingMap.kt"
    val blockDataFileName = "BlockData.kt"
    val srcOutputDir = File(".", "src/main/kotlin")
    val outputDir = File(srcOutputDir, "generated")
    val processorSrc = File(".", "game-files-processor/src/main/kotlin")
    if( !outputDir.mkdirs() && !outputDir.isDirectory) {
        throw IOException("couldn't create output dir for sbc files")
    }
    //copy over needed kotlin files to js module
    val countingMapFile = File(processorSrc, countingMapFileName)
    val blockDataFile = File(processorSrc, blockDataFileName)

    countingMapFile.copyTo(File(srcOutputDir, countingMapFileName), overwrite = true)
    blockDataFile.copyTo(File(srcOutputDir, blockDataFileName), overwrite = true)

    val outputFile = File(outputDir, "data.kt")
    outputFile.writeText("package generated\nimport BlockData\n")
    outputFile.appendText(allBlockData.fold("val data=listOf(") { acc, blockData: BlockData ->
        "$acc$blockData, "
    }+")")
}

fun main() {
    initComponents()
    initLocalisation()
    initCubeBlockDefinitions()
    println("all block data:"+allBlockData.size)
    println("number of components where the xsiType is the same as the typId: $identical")
    println("number of components where the xsiType is empty: $empty")
    //println("power tags: $powerTags")
    writeItAllOut()
}