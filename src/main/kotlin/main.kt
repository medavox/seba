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
// time to accelerate 100m/s (using forward thrusters) -- and time to decelerate to 0m/s (using backward thrusters)
// seconds of thrust until depleted (hydrogen tanks and/or electricity)
val unfoundBlocksList = document.getElementById("unfound_blocks_list") as HTMLUListElement

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
            blueprintXmlDoc.processBlueprint()
            Unit //it's not redundant because kotlin/js is being doopid
        }
    })
    document.setupCollapsibleButton("breakdown_button", "breakdown_div")
    document.setupCollapsibleButton("totals_button", "totals_div")
}
