import kotlinx.browser.document
import org.w3c.dom.*

/**Functions which operate on a blueprint file's XML*/

private fun XMLDocument.getBlueprintName(): String = querySelector("Definitions > ShipBlueprints > ShipBlueprint > Id"
)?.attributes?.get("Subtype")?.value ?: "<unknown name>"

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

private fun Element.getGridName(): String = firstChildElementWithTag("DisplayName")?.textContent ?: "<no name found>"

private fun Element.getGridSizeFromGrid(): GridSize? {
    return when (firstChildElementWithTag("GridSizeEnum")?.textContent) {
        "Large" -> GridSize.LARGE
        "Small" -> GridSize.SMALL
        else -> null
    }
}

private fun Element.countBlocksInGrid(): CountingMap<String> {
    val gridBlocks:List<Element> = firstChildElementWithTag("CubeBlocks")?.children?.asList() ?: throw NoSuchElementException("couldn't find blocks in grid")
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

fun XMLDocument.processBlueprint() {
    //in Definitions > ShipBlueprint > CubeGrids:
    val cubeGridsElement: Element = querySelector("Definitions > ShipBlueprints > ShipBlueprint > CubeGrids")
        ?: throw Exception("couldn't find CubeGrids element!")
    val grids = cubeGridsElement.childElementsWithTag("CubeGrid")
    println("${cubeGridsElement.childNodes.length} grids in blueprint:"+grids.joinToString(separator = "\n") {
        it.getGridName()
    })
    document.getElementById("blueprintName")?.textContent = "Stats for '${getBlueprintName()}'"
    val blockCountByGrid = mutableMapOf<String, CountingMap<String>>()
    val totalBlockCounts = CountingMap<String>()

    val total = Totals()

    for(grid in grids) {
        when(grid.getGridSizeFromGrid()) {
            GridSize.LARGE -> total.largeGrids++
            GridSize.SMALL -> total.smallGrids++
            else -> throw Exception("couldn't find grid size for grid '${grid.getGridName()}'!")
        }
        val counts:CountingMap<String> = grid.countBlocksInGrid()
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


    blockDataCounts.entries.forEach { (blockData:BlockData, count: Int) ->
        total.mass += (count * blockData.mass)
        total.pcu += (count * blockData.pcu)

        when (blockData.gridSize) {
            GridSize.LARGE -> total.largeBlocks += count
            GridSize.SMALL -> total.smallBlocks += count
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
    var smallGrids: Int = 0,
    var largeGrids: Int = 0,
)

//totals: total grids (including root/main grid); number of small grids, number of large grids

//name?, blocks, mass, pcu, small or large grid
