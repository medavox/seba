import generated.data
import org.w3c.dom.parsing.DOMParser
import kotlinx.browser.document
import kotlinx.dom.*
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.files.File
import org.w3c.files.FileReader

//TODO: power consumption:
// values are negative for consumption, positive for generation
// max power capacity
// time til drained when everything is running at full and no generators running
// can run indefinitely with everything on idle?
// can we meet max power demand? (is total max output >= max demand)
//      with just batteries?
// print grid name
// total mass
// total blocks
// total PCU
// match blocks also by grid size
// display info including subgrids, without subgrids, and breakdown per grid
val unfoundBlocksList = document.getElementById("unfound_blocks_list") as HTMLUListElement

fun processBlueprint(blueprint: XMLDocument): CountingMap<String> {
    //in Definitions > ShipBlueprint > CubeGrids:
    val cubeGrids: Element = blueprint.querySelector("Definitions > ShipBlueprints > ShipBlueprint > CubeGrids")
        ?: throw Exception("couldn't find CubeGrids element!")
    println("number of grids in blueprint:"+cubeGrids.childNodes.length)
    val mainGrid:Element = cubeGrids.firstElementChild ?: throw Exception("couldn't get first (presumably main) grid!")
    val mainGridBlocks:List<Element> = mainGrid.firstChildElementWithTag("CubeBlocks").children.asList()
    println("number of blocks in main grid:"+mainGridBlocks.size)
    //right, now let's get to work
    val blockCounts = CountingMap<String>()
    val blueprintName: String = blueprint.querySelector("Definitions > ShipBlueprints > ShipBlueprint > Id"
        )?.attributes?.get("Subtype")?.value ?: "<unknown name>"
    document.getElementById("blueprintName")?.textContent = "Stats for $blueprintName"
    for(blockElement in mainGridBlocks) {
        val subtype = blockElement.firstChildElementWithTag("SubtypeName").textContent ?: throw Exception("block has no subtype!")
        val xsiType = blockElement.attributes.get("xsi:type")?.value?.replace("MyObjectBuilder_", "")?: ""

        blockCounts["$xsiType/$subtype"] += 1
        /*val blockData = data.first {
            it.subtypeId == subtype
        }*/
    }
    val dataRows = blockCounts.mapNotNull { (xsiSub, count) ->
        createBlockRow(xsiSub, count)
    }
    showBreakdown(dataRows.sortedBy { it.name })
    return blockCounts
}

fun createBlockRow(xsiSub: String, count: Int): BlockRow? {
    println("xsisub: $xsiSub")
    //so it looks like the xsi:Type in a blueprint ACTUALLY corresponds to the typeId
    val block:BlockData? = data.firstOrNull { (it.typeId+"/"+it.subtypeId) == xsiSub }
    if(block == null) {
        document.getElementById("unfound_blocks")?.addClass("vizzibull")
        unfoundBlocksList.appendElement("li") {
            appendText(xsiSub)
        }
        return null
    }
    return BlockRow(
        name = block.humanName,
        count = count,
        mass = count * block.mass,
        pcu = count * block.pcu
    )
}

private fun Element.childElementsWithTag(tag: String): List<Element> {
    return this.children.asList().filter {
        println("child name: "+it.tagName)
        it.tagName == tag
    }
}

private fun Element.firstChildElementWithTag(tag: String): Element {
    return this.children.asList().first {
        it.tagName == tag
    }
}

fun main() {
    val blueprintFileInput = document.getElementById("blueprint_file_input") as HTMLInputElement

    //once the user provides a blueprint,
    // load its contents and enable the rest of the page UI
    blueprintFileInput.addEventListener("change", { event ->
        val blueprint:File = event.target.asDynamic().files[0] as File

        println("file: ${blueprint.name}; size: ${blueprint.size}")
        val fr = FileReader()
        fr.readAsText(blueprint)
        fr.onload = { loadedEvent ->
            val blueprintXmlDoc = DOMParser().parseFromString(loadedEvent.target.asDynamic().result as String,
                    "text/xml") as XMLDocument
            val parseError = blueprintXmlDoc.querySelector("parsererror")
            parseError?.let {

            }
            processBlueprint(blueprintXmlDoc)
            Unit //it's not redundant because kotlin/js is being doopid
        }
    })
}

/**called when the user presses the button to initiate the search*/
fun showBreakdown(tableData:List<BlockRow>) {
    val breakdownTable = document.getElementById("breakdown") as HTMLTableElement
    breakdownTable.clear()
    breakdownTable.appendElement("tr") {
        //name, count, mass, pcu
        appendElement("th") {appendText("Name")}.addEventListener("click", {
            showBreakdown(tableData.sortedBy { it.name })
        })
        appendElement("th") {appendText("Count")}.addEventListener("click", {
            showBreakdown(tableData.sortedByDescending { it.count })
        })
        appendElement("th") {appendText("Mass (kg)")}.addEventListener("click", {
            showBreakdown(tableData.sortedByDescending { it.mass })
        })
        appendElement("th") {appendText("PCU")}.addEventListener("click", {
            showBreakdown(tableData.sortedByDescending { it.pcu })
        })
    }
    for(row in tableData) {
        row.toHtml(breakdownTable)
    }

    (document.getElementById("results") as HTMLDivElement).addClass("vizzibull")
}
