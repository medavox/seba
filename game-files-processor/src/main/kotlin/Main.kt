import generated.Components
import generated.MyTexts
import generated.cubeBlocksList
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

private val allBlockData: MutableList<BlockData> = mutableListOf()
private val localisationStringsMappings = mutableMapOf<String, String>()
private val powerTags = mutableSetOf<String>()
private val components = mutableMapOf<String, Double>()

/**Blocks in CubeBlocks_Armor_2 are missing their PCU entry. But in the game it's 1. So just return 1*/
fun Element.getPcuWithFallbackForArmor2(file: String, subtypeId: String): Int? {
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

fun initComponents() {
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

fun initLocalisation() {
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
        localisationStringsMappings[key] = value
    }
}
//passage is the only block whose subtypeid is blank
fun initCubeBlockDefinitions() {
    val cubeBlocksXmlDocs: Map<String, Element> = cubeBlocksList.mapValues {
        Jsoup.parse(it.value).root()
    }

    for((fileName, definitionsFile) in cubeBlocksXmlDocs) {
        val blockDefs: Element = definitionsFile.select("Definitions>CubeBlocks")
            .firstOrNull()?: throw Exception ("Block defs not found")
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
            val humanName: String = localisationStringsMappings[displayName.ownText()] ?: subtypeId.ownText()
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

            allBlockData.add(BlockData(
                type = typeId.ownText(),
                subtypeId = subtypeId.ownText(),
                pcu = pcu,
                displayName = displayName.ownText(),
                humanName = humanName,
                components = CountingMap(components.toMutableMap())
            ))
        }
    }
}

fun main() {
    initLocalisation()
    initCubeBlockDefinitions()
    initComponents()
    println("all block data:"+allBlockData.size)
    println("power tags: $powerTags")
    //println(allBlockDefs[451])
}