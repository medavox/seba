import generated.data
import org.w3c.dom.parsing.DOMParser
import kotlinx.browser.document
import kotlinx.dom.*
import org.w3c.dom.*
import org.w3c.files.File
import org.w3c.files.FileReader

//TODO: power consumption:
// values are negative for consumption, positive for generation
// max power capacity
// time til drained when everything is running at full and no generators running
// can run indefinitely with everything on idle?
// can we meet max power demand? (is total max output >= max demand)
//      with just batteries?
// display info including subgrids, without subgrids, and breakdown per grid
// per-grid breakdown, percent of total ship
// breakdown of blocks from DLCs: name, number & DLC of each
// function to replace all DLC blocks in grid with non-DLC equivalents
val unfoundBlocksList = document.getElementById("unfound_blocks_list") as HTMLUListElement

fun XMLDocument.getName(): String = querySelector("Definitions > ShipBlueprints > ShipBlueprint > Id"
)?.attributes?.get("Subtype")?.value ?: "<unknown name>"

fun processBlueprint(blueprint: XMLDocument) {
    //in Definitions > ShipBlueprint > CubeGrids:
    val cubeGridsElement: Element = blueprint.querySelector("Definitions > ShipBlueprints > ShipBlueprint > CubeGrids")
        ?: throw Exception("couldn't find CubeGrids element!")
    val grids = cubeGridsElement.childElementsWithTag("CubeGrid")
    println("${cubeGridsElement.childNodes.length} grids in blueprint:"+grids.joinToString(separator = "\n") {
        it.getGridName()
    })
    document.getElementById("blueprintName")?.textContent = "Stats for ${blueprint.getName()}"
    val blockCountByGrid = mutableMapOf<String, CountingMap<String>>()
    val totalBlockCounts = CountingMap<String>()

    for(grid in grids) {
        val counts:CountingMap<String> = countBlocksInGrid(grid)
        totalBlockCounts += counts
        val gridName = grid.getGridName()
        if(!blockCountByGrid.containsKey(gridName)) {
            blockCountByGrid.put(grid.getGridName(), counts)
        } else {
            blockCountByGrid.put(grid.getGridName()+" copy", counts)
        }
    }

    println("totalBlockCounts size:"+totalBlockCounts.size)
    val blockDataCounts:Map<BlockData, Int> = totalBlockCounts.mapToBlockData()
    println("blockDataCounts size:"+blockDataCounts.size)
    val total = Totals()


    blockDataCounts.entries.forEach { (blockData:BlockData, count: Int) ->
        total.mass += (count * blockData.mass)
        total.pcu += (count * blockData.pcu)

        when (blockData.size) {
            'L' -> total.largeBlocks += count
            'S' -> total.smallBlocks += count
            else -> throw Exception("wtf blocksize is somehow still not 'L' or 'S' for ${blockData.humanName}! " +
                    "it's ${blockData.size}")
        }
    }

    val dataRows = blockDataCounts.map { (block, count) ->
        BlockRow(
            name = block.humanName,
            count = count,
            mass = count * block.mass,
            pcu = count * block.pcu
        )
    }
    document.populateBreakdownTable(dataRows.sortedBy { it.name })

    //populate totals table
    val totalsTable = document.getElementById("totals_table") as HTMLTableElement
    totalsTable.populateTotalsTable(total)
}

data class Totals(
    var mass: Double = 0.0,
    var pcu: Int = 0,
    var smallBlocks: Int = 0,
    var largeBlocks: Int = 0,
)

fun Element.getGridName(): String = firstChildElementWithTag("DisplayName")?.textContent ?: "<no name found>"

fun countBlocksInGrid(grid: Element): CountingMap<String> {

    val gridBlocks:List<Element> = grid.firstChildElementWithTag("CubeBlocks")?.children?.asList() ?: throw NoSuchElementException("couldn't find blocks in grid")
    println("number of blocks in grid:"+gridBlocks.size)
    //right, now let's get to work
    val blockCounts = CountingMap<String>()

    for(blockElement in gridBlocks) {
        val subtype = blockElement.firstChildElementWithTag("SubtypeName")?.textContent ?: throw Exception("block has no subtype!")
        val xsiType = blockElement.attributes.get("xsi:type")?.value?.replace("MyObjectBuilder_", "")?: ""

        blockCounts["$xsiType/$subtype"] += 1
        //println("blockCounts[$xsiType/$subtype] = "+blockCounts["$xsiType/$subtype"])
    }
    return blockCounts
}

fun CountingMap<String>.mapToBlockData(): Map<BlockData, Int> {
    val output = mutableMapOf<BlockData, Int>()
    this.forEach { (xsiSub, count) ->
        //so it looks like the xsi:Type in a blueprint ACTUALLY corresponds to the typeId
        val matches = data.filter { (it.typeId + "/" + it.subtypeId) == xsiSub }
        if(matches.size != 1) {
            println("ERROR: ${matches.size} matches found for $xsiSub")
        }
        val block: BlockData? = matches.firstOrNull()
        if (block == null) {
            document.getElementById("unfound_blocks")?.addClass("vizzibull")
            unfoundBlocksList.appendElement("li") {
                appendText(xsiSub)
            }
        } else {
            output.put(block, count)
        }
    }
    return output
}

private fun Element.childElementsWithTag(tag: String): List<Element> {
    return this.children.asList().filter {
        println("child name: "+it.tagName)
        it.tagName == tag
    }
}

private fun Element.firstChildElementWithTag(tag: String): Element? {
    return this.children.asList().firstOrNull {
        it.tagName == tag
    }
}

fun main() {
    val blueprintFileInput = document.getElementById("blueprint_file_input") as HTMLInputElement
    document.getElementById("noscript")?.remove()
    //once the user provides a blueprint,
    // load its contents and enable the rest of the page UI
    blueprintFileInput.addEventListener("change", { event ->
        val blueprint:File = event.target.asDynamic().files[0] as File
        document.resetPage()
        println("file: ${blueprint.name}; size: ${blueprint.size}")
        val fr = FileReader()
        fr.readAsText(blueprint)
        fr.onload = { loadedEvent ->
            val blueprintXmlDoc = DOMParser().parseFromString(loadedEvent.target.asDynamic().result as String,
                    "text/xml") as XMLDocument
            val parseError = blueprintXmlDoc.querySelector("parsererror")
            parseError?.let {
                document.getElementById("invalid_file")?.addClass("vizzibull")
            }
            processBlueprint(blueprintXmlDoc)
            Unit //it's not redundant because kotlin/js is being doopid
        }
    })
    document.setupCollapsibleButton("breakdown_button", "breakdown_div")
    document.setupCollapsibleButton("totals_button", "totals_div")
}
