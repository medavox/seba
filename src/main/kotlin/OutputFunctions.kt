import kotlinx.dom.*
import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTableElement

/**Functions which operate on the HTML of the web app; that is, produce output in the web app.*/

fun Document.setupCollapsibleButton(buttonId: String, contentDivId: String) {
    val button = this.getElementById(buttonId) as HTMLButtonElement
    val div = this.getElementById(contentDivId) as HTMLDivElement
    button.addEventListener("click", { event ->
        println("bblep")
        button.classList.toggle("active")
        println("table height:"+div.style.maxHeight)
        if (button.classList.contains("active")) {
            div.style.maxHeight = div.scrollHeight.toString() + "px"
        } else {
            div.style.maxHeight = "0px"
        }
    })
}

fun Document.populateBreakdownTable(tableData:List<BlockRow>) {
    val div = this.getElementById("breakdown_div") as HTMLDivElement
    val breakdownTable = this.getElementById("breakdown_table") as HTMLTableElement
    breakdownTable.clear()
    breakdownTable.appendElement("tr") {
        //name, count, mass, pcu
        appendElement("th") {appendText("Name")}.addEventListener("click", {
            populateBreakdownTable(tableData.sortedBy { it.name })
        })
        appendElement("th") {appendText("Count")}.addEventListener("click", {
            populateBreakdownTable(tableData.sortedByDescending { it.count })
        })
        appendElement("th") {appendText("Mass (kg)")}.addEventListener("click", {
            populateBreakdownTable(tableData.sortedByDescending { it.mass })
        })
        appendElement("th") {appendText("PCU")}.addEventListener("click", {
            populateBreakdownTable(tableData.sortedByDescending { it.pcu })
        })
    }
    for(row in tableData) {
        row.toHtml(breakdownTable)
    }

    (this.getElementById("results") as HTMLDivElement).addClass("vizzibull")
    div.style.maxHeight = div.scrollHeight.toString() + "px"
}

fun HTMLTableElement.populateTotalsTable(total: Totals) {
    clear()
    appendElement("tr") {
        appendElement("td") {
            appendElement("p") { appendText(
                "NOTE: these numbers may be higher than the in-game Info Screen, " +
                        "because that screen doesn't account for subgrids.")
            }
            appendElement("p") {appendText(
                "The Info Screen mass also includes the contents of any inventories, eg ore, spare components etc.")
            }
        }
    }
    appendElement("tr") {
        appendElement("td") { appendText("Total Mass") }
        appendElement("td") { appendText(total.mass.asDynamic().toLocaleString() as String) }
    }
    appendElement("tr") {
        appendElement("td") { appendText("Total PCU") }
        appendElement("td") { appendText(total.pcu.asDynamic().toLocaleString() as String) }
    }
    appendElement("tr") {
        appendElement("td") { appendText("Total Blocks") }
        appendElement("td") { appendText((total.largeBlocks + total.smallBlocks).asDynamic().toLocaleString() as String) }
    }
    appendElement("tr") {
        appendElement("td") { appendText("Small Blocks") }
        appendElement("td") { appendText(total.smallBlocks.asDynamic().toLocaleString() as String) }
    }
    appendElement("tr") {
        appendElement("td") { appendText("Large Blocks") }
        appendElement("td") { appendText(total.largeBlocks.asDynamic().toLocaleString() as String) }
    }
    appendElement("tr") {
        appendElement("td") { appendText("Total Grids (including root/main grid)") }
        appendElement("td") { appendText((total.smallGrids + total.largeGrids).asDynamic().toLocaleString() as String) }
    }
    appendElement("tr") {
        appendElement("td") { appendText("Total Large Grids") }
        appendElement("td") { appendText(total.largeGrids.asDynamic().toLocaleString() as String) }
    }
    appendElement("tr") {
        appendElement("td") { appendText("Total Small Grids") }
        appendElement("td") { appendText(total.smallGrids.asDynamic().toLocaleString() as String) }
    }
}

fun Document.resetPage() {
    getElementById("unfound_blocks")?.removeClass("vizzibull")
    getElementById("invalid_file")?.removeClass("vizzibull")
    getElementById("unfound_blocks_list")?.clear()
}