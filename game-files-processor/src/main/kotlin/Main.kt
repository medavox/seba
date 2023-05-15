import generated.Components
import generated.MyTexts
import generated.cubeBlocksList
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.io.IOException

val allBlockData: MutableList<BlockData> = mutableListOf()
val localisationStrings = mutableMapOf<String, String>()
private val powerTags = mutableSetOf<String>()
val components = mutableMapOf<String, Double>()

/**Blocks in CubeBlocks_Armor_2 are missing their PCU entry. But in the game it's 1. So just return 1*/
private fun Element.getPcuWithFallbackForArmor2(file: String, subtypeId: String): Int? {
    //in future, we may have to add manually-looked-up PCU values from the game here
/*    val manualPcuLookups = mapOf(
        "CubeBlock/DeadAstronaut" to
    )*/
    val pcu = this.getElementsByTag("PCU").firstOrNull()?.ownText()?.toInt()
        ?: return if(file == "CubeBlocks_Armor_2") 1
/*        else if(false) {
            null
        } */else {
            null
        }
    return pcu
}

private fun initComponents() {
    val doc = Jsoup.parse(Components).root()
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
    val doc = Jsoup.parse(MyTexts).root()
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

private fun Map<String, Int>.calculateMass(): Double = entries.fold(0.0) { acc, elem ->
    acc + elem.value * (components.get(elem.key) ?: 0.0)
}

private fun initCubeBlockDefinitions() {
    val cubeBlocksXmlDocs: Map<String, Element> = cubeBlocksList.mapValues {
        Jsoup.parse(it.value).root()
    }

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
            val subtypeId = block.getElementsByTag("SubtypeId")
                .firstOrNull()?: throw Exception("couldn't find subtype id in $fileName")
            val typeId = block.getElementsByTag("TypeId")
                .firstOrNull()?: throw Exception("couldn't find type id in $fileName")
            val displayName = block.getElementsByTag("DisplayName").firstOrNull()
            if(displayName == null) {
                println("WARNING: couldn't find DisplayName for '${subtypeId.ownText()}' in $fileName")
                continue
            }
            val humanName: String = localisationStrings[displayName.ownText()] ?: subtypeId.ownText()
            val pcu: Int? = block.getPcuWithFallbackForArmor2(fileName, subtypeId.ownText())
            if(pcu == null) {
                println("WARNING: couldn't find PCU for '${typeId.ownText()}/${subtypeId.ownText()}/$humanName' in $fileName")
                continue
            }

            val componentsRaw = block.getElementsByTag("Components").firstOrNull()?.children()?:
                throw Exception("couldn't find Components for '${typeId.ownText()}/${subtypeId.ownText()}/$humanName' in $fileName")

            val components:Map<String, Int> = componentsRaw.associate { component ->
                component.attr("Subtype") to component.attr("Count").toInt()
            }

            val xsiType = block.attr("xsi:type")

            allBlockData.add(BlockData(
                type = typeId.ownText(),
                subtypeId = subtypeId.ownText(),
                pcu = pcu,
                displayName = displayName.ownText(),
                humanName = humanName,
                components = components,
                mass = components.calculateMass(),
                xsiType = xsiType
            ))
        }
    }
}


private fun writeItAllOut() {
    val outputDir = File(".", "src/main/kotlin/generated_data")
    if( !outputDir.mkdirs() && !outputDir.isDirectory) {
        throw IOException("couldn't create output dir for sbc files")
    }
    val outputFile = File(outputDir, "data.kt")
    outputFile.writeText("package generated_data\n\n")
    outputFile.appendText(allBlockData.fold("val data=listOf(") { acc, blockData: BlockData ->
        "$acc$blockData, "
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
    initLocalisation()
    initCubeBlockDefinitions()
    initComponents()
    println("all block data:"+allBlockData.size)
    //println("power tags: $powerTags")
    writeItAllOut()
}