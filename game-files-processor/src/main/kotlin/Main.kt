import generated.cubeBlocksList
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

private val allBlockDefs: MutableList<Element> = mutableListOf()
private val allBlockData: MutableList<BlockData> = mutableListOf()
fun init() {
    val cubeBlocksXmlDocs: List<Element> = cubeBlocksList.map {
        Jsoup.parse(it).root()
    }
    for(definitionsFile in cubeBlocksXmlDocs) {
        val blockDefs: Element = definitionsFile.select("Definitions>CubeBlocks").firstOrNull()?: throw Exception ("Block defs not found")
        println("block defs:"+blockDefs.children().size)
        for (block in blockDefs.children()) {
            val subtypeId = block.getElementsByTag("SubtypeId").firstOrNull()?: throw Exception("couldn't find subtype id")
            val pcu = block.getElementsByTag("PCU").firstOrNull()?: throw Exception("couldn't find PCU for ${block.tagName()}")
            val displayName = block.getElementsByTag("DisplayName").firstOrNull()?: throw Exception("couldn't find DisplayName")
            allBlockData.add(BlockData(
                subtypeId = subtypeId.ownText(),
                pcu = pcu.ownText().toInt(),
                displayName = displayName.ownText()
            ))
        }
        allBlockDefs.addAll(blockDefs.children())
    }
}

fun main() {
    init()
    println("all block data:"+allBlockData.size)
    //println(allBlockDefs[451])
}