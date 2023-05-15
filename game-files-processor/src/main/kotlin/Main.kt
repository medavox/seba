import generated.MyTexts
import generated.cubeBlocksList
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

private val allBlockData: MutableList<BlockData> = mutableListOf()
private val localisationStringsMappings = mutableMapOf<String, String>()

/**Blocks in CubeBlocks_Armor_2 are missing their PCU entry. But in the game it's 1. So just return 1*/
fun Element.getPcuWithFallbackForArmor2(file: String, subtypeId: String): Int? {
    /*val manualPcuLookups = mapOf(
        "LadderShaft" to
    )*/
    val pcu = this.getElementsByTag("PCU").firstOrNull()?.ownText()?.toInt()
        ?: return if(file == "CubeBlocks_Armor_2") 1
        else if(false) {
            null
        } else {
            null
        }
    return pcu
}
fun initLocalisationStringMappings() {
    val doc = Jsoup.parse(MyTexts).root()
    val entries = doc.getElementsByTag("data")
    println("i18n entries: ${entries.size}")game-files-processor/build.gradle.kts
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

    for((file, definitionsFile) in cubeBlocksXmlDocs) {
        val blockDefs: Element = definitionsFile.select("Definitions>CubeBlocks").firstOrNull()?: throw Exception ("Block defs not found")
        //println("${blockDefs.children().size} block defs in $file")
        for (block in blockDefs.children()) {
            //I thought I could uniquely identify all blocks by just their subtypeid, but no
            val subtypeId = block.getElementsByTag("SubtypeId").firstOrNull()?: throw Exception("couldn't find subtype id in $file")
            val typeId = block.getElementsByTag("TypeId").firstOrNull()?: throw Exception("couldn't find type id in $file")
            val pcu: Int? = block.getPcuWithFallbackForArmor2(file, subtypeId.ownText())
            if(pcu == null) {
                println("WARNING: couldn't find PCU for '${subtypeId.ownText()}' in $file")
                continue
            }
            val displayName = block.getElementsByTag("DisplayName").firstOrNull()
            if(displayName == null) {
                println("WARNING: couldn't find DisplayName for '${subtypeId.ownText()}' in $file")
                continue
            }
            allBlockData.add(BlockData(
                type = typeId.ownText(),
                subtypeId = subtypeId.ownText(),
                pcu = pcu,
                displayName = displayName.ownText(),
            ))
        }
    }
}

fun main() {
    initLocalisationStringMappings()
    println("all block data:"+allBlockData.size)
    //println(allBlockDefs[451])
}