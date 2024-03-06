import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * This file extracts the needed data (names, mass, PCU, components to build) from the game SBC files,
 * and outputs it as programmatically-generated Kotlin into the file `<root>/src/main/kotlin/generated/data.kt`.
 *
 * It declares a data class for every CubeBlock in the game, containing the pertinent stats above.
 *
 * It also copies the files containing the BlockData and CountingMap classes over to the web app module.
 * Not a pretty solution, but much more straightforward and easy to understand (and therefore maintain) than using gradle artifacts.
 *
 * All this is run on a desktop JVM before the actual web app is even compiled,
 * so performance is less of a requirement than in the web context.
 *
 * It's a totally separate execution environment from the web app itself.*/

private val resDonor = object{}
val allBlockData: MutableList<BlockData> = mutableListOf()
val allRecipeData: MutableList<RecipeData> = mutableListOf()
val localisationStrings = mutableMapOf<String, String>()
val components = mutableMapOf<String, Double>()
val dlcBlockCounts:CountingMap<String> = mutableMapOf()

var identical = 0//number of components where the xsiType is the same as the typId
var empty = 0//number of components where the xsiType is empty
// railgun seems to be missing its recharge power data.
// antenna is missing a powerinput or any other
// possible leads: WeaponDefinitionId, ResourceSinkGroup, InventoryFillFactorMin

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

private fun initRecipeData():List<RecipeData> {
    val doc = Jsoup.parse(readResource("Blueprints.sbc")).root()
    val entries = doc.getElementsByTag("Blueprint")
    println("component entries: ${entries.size}")
    for(entry in entries) {
        val recipeName = entry.getElementsByTag("SubtypeId").firstOrNull()?.ownText()
            ?: throw Exception("couldn't find subtype id for a component!")
        val displayName = entry.getElementsByTag("DisplayName").firstOrNull()?.ownText()
            ?: throw Exception("couldn't find display name for component $recipeName!")

        val prodTime = entry.getElementsByTag("BaseProductionTimeInSeconds").firstOrNull()?.ownText()
            ?: throw Exception("couldn't find production time for component $recipeName!")

        //skip recipes which produce multiple results, as they're stone recipes we don't care about (atm)
        val resultsWhichShouldBeNull = entry.getElementsByTag("Results").firstOrNull()
        if(resultsWhichShouldBeNull != null) continue

        val result = entry.getElementsByTag("Result").firstOrNull()
            ?: throw Exception("couldn't find result name for component $recipeName!")

        val prereqs = entry.getElementsByTag("Prerequisites").firstOrNull()?.children()
            ?: throw Exception("couldn't find prereqs for component $recipeName!")

        println("prereqs elem for '$recipeName': ${prereqs.size}")

        val prereqsMapx1000:Map<String, Int> = prereqs.associate { item ->
            item.attr("SubtypeId") to
                    (item.attr("Amount").toDoubleOrNull()?.times(1000)?:0).toInt()
        }

        println("prereqs for '$recipeName': ${prereqsMapx1000.size}")

        allRecipeData.add(RecipeData(
            humanName = localisationStrings[displayName] ?: "<null>",
            recipeName = recipeName,
            resultName = result.attr("SubtypeId"),
            displayName = displayName,
            recipeAmountsx1000 =  prereqsMapx1000,
            resultAmountx1000 = (result.attr("Amount").toDoubleOrNull()?.times(1000) ?: 0).toInt(),
            productionTimeMs = (prodTime.toDoubleOrNull()?.times(1000) ?: -1).toInt()
        ))
    }
    return allRecipeData
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
                println("WARNING: couldn't find PCU for '$xsiTypeSub', $blockSize block in $fileName")
                continue
            }

            if(typeId.ownText().contains("_")) {
                println("weird typeId for subType '$subtypeId': ${typeId.ownText()}")
            }

            val componentsRaw = block.getElementsByTag("Components").firstOrNull()?.children()?:
            throw Exception("couldn't find Components for '$xsiTypeSub' in $fileName")

            val components: CountingMap<String> = mutableMapOf()
            componentsRaw.forEach { component ->
                components.addCount(component.attr("Subtype"), component.attr("Count").toInt())
            }

            val xsiType = block.attr("xsi:type")
                .replace("Definition", "")
                .replace("MyObjectBuilder_", "")

            if(xsiType == typeId.ownText()) {
                identical++
            } else if(xsiType.isEmpty()) {
                empty++
            } else {
                //println("xsiType for $humanName is $xsiType")
            }

            val dlc = block.getElementsByTag("DLC").firstOrNull()?.ownText()
            dlc?.let {
//                dlcBlockCounts.put(dlc, dlcBlockCounts.get(dlc)+1)
                dlcBlockCounts.addCount(dlc, 1)
            }

            val sizeAttrs = block.getElementsByTag("Size").firstOrNull()?.attributes()
            val x = sizeAttrs?.get("x")?.toIntOrNull()
            val y = sizeAttrs?.get("y")?.toIntOrNull()
            val z = sizeAttrs?.get("z")?.toIntOrNull()


            allBlockData.add(BlockData(
                typeId = typeId.ownText().replace("MyObjectBuilder_", ""),
                subtypeId = subtypeId,
                pcu = pcu,
                dlc = dlc,
                humanName = humanName,
                gridSize = if(blockSize == "Large") GridSize.LARGE else GridSize.SMALL,
                dimensions = if(x != null && y != null && z != null) BlockSize(x, y, z) else null,
                components = components,
                mass = components.calculateMass(),
                xsiType = xsiType,
            ))
        }
    }
    println("blocks in each DLC:")
    println(dlcBlockCounts.entries.joinToString(separator = "\n") { (key, value) -> "\t$key: $value"  })
    println("total DLC blocks: "+dlcBlockCounts.values.sum())
}

private fun writeItAllOut() {
    val countingMapFileName = "CountingMap.kt"
    val blockDataFileName = "BlockData.kt"
    val recipeDataFileName = "RecipeData.kt"

    val srcOutputDir = File(".", "src/main/kotlin")
    val outputDir = File(srcOutputDir, "generated")
    val processorSrc = File(".", "game-files-processor/src/main/kotlin")
    if( !outputDir.mkdirs() && !outputDir.isDirectory) {
        throw IOException("couldn't create output dir for sbc files")
    }
    //copy over needed kotlin files to js module
    val countingMapFile = File(processorSrc, countingMapFileName)
    val blockDataFile = File(processorSrc, blockDataFileName)
    val recipeDataFile = File(processorSrc, recipeDataFileName)

    countingMapFile.copyTo(File(srcOutputDir, countingMapFileName), overwrite = true)
    blockDataFile.copyTo(File(srcOutputDir, blockDataFileName), overwrite = true)
    recipeDataFile.copyTo(File(srcOutputDir, recipeDataFileName), overwrite = true)

    val dataOutputFile = File(outputDir, "data.kt")
    dataOutputFile.writeText("""package generated
        |import BlockData
        |import BlockSize
        |import GridSize
        |""".trimMargin())
    dataOutputFile.appendText(allBlockData.fold("val data=listOf(") { acc, blockData: BlockData ->
        "$acc$blockData, "
    }+")")

    val recipesOutputFile = File(outputDir, "recipes.kt")
    recipesOutputFile.writeText("""package generated
        |import RecipeData
        |""".trimMargin())
    recipesOutputFile.appendText(allRecipeData.fold("val recipes=listOf(") { acc, recipeData: RecipeData ->
        "$acc$recipeData, "
    }+")")
}

//todo: blueprints only include the subtypeId, which as we've discovered isn't always enough to uniquely identify a block,
//  because it's sometimes blank (eg for both types of gravity generator)
//  so our only other option for a string that shows up in BOTH the blueprint AND in the CubeBlock <Definition>,
//  is the xsi:type. But it's not identical between the two, just derivable.
//  in a Blueprint:
//  <MyObjectBuilder_CubeBlock xsi:type="MyObjectBuilder_GravityGeneratorSphere">
//              <SubtypeName />
//  ...
// and in its Definition:
//         <Definition xsi:type="MyObjectBuilder_GravityGeneratorSphereDefinition">
//            <Id>
//                <TypeId>GravityGeneratorSphere</TypeId>
//                <SubtypeId />
//            </Id>
// so it looks like we'll have ot include the xsi:type in the BlockData, but chop off the word Definition from the end of the attribute value
//  update:
// not every CubeBlock <Definition> has an xsi:Type :(
// some of the definitions that don't have a subtype, DO have an xsiType -- but not all of them!
// in Interiors:
// passage (Interiors:7) has neither an xsiType nor a subtypeId
// ladder2 (Interiors:820) is the same. Are they used in the game? probably!
// so in this case, the last (LAST) option seems to be:
// take the xsi:Type value from the blueprint (eg MyObjectBuilder_Passage),
// strip off the "MyObjectBuilder_" prefix,
// search for the result as a type id


//xsi:Type values aren't unique!

//so to sum up, it's a 3-stage search:
// try looking by subtype.
// if the blueprint block's subtype is empty,
// try looking by xsiType.
// and if THAT fails,
// then it's either a passage or ladder2, so use the

fun main() {
    //the ordering of these init functions matters,
    // because the later ones rely on data initialised in earlier ones
    initComponents()
    initLocalisation()
    initRecipeData()
    initCubeBlockDefinitions()
    println("all block data:"+allBlockData.size)
    println("number of components where the xsiType is the same as the typId: $identical")
    println("number of components where the xsiType is empty: $empty")
    //println("power tags: $powerTags")
    writeItAllOut()
}