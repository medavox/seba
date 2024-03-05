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

fun Document.populateBreakdownTable(tableData:List<BreakdownRow>) {
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
        appendElement("td") { appendText("Total Mass (kg)") }
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

fun Document.populateSubgridsTable(tableData:List<BreakdownRow>) {
    val div = this.getElementById("subgrids_div") as HTMLDivElement
    val subgridsTable = this.getElementById("subgrids_table") as HTMLTableElement
    subgridsTable.clear()

    //name?, blocks, mass, pcu, small or large grid, % total blocks, % total mass, % total PCU
    subgridsTable.appendElement("tr") {
        //name, count, mass, pcu
        appendElement("th") {appendText("Name")}.addEventListener("click", {
            populateSubgridsTable(tableData.sortedBy { it.name })
        })
        appendElement("th") {appendText("Grid Size")}.addEventListener("click", {
            populateSubgridsTable(tableData.sortedBy { it.name })
        })
        appendElement("th") {appendText("Blocks")}.addEventListener("click", {
            populateSubgridsTable(tableData.sortedByDescending { it.count })
        })
        appendElement("th") {appendText("% Total Blocks")}.addEventListener("click", {
            populateSubgridsTable(tableData.sortedByDescending { it.count })
        })
        appendElement("th") {appendText("Mass (kg)")}.addEventListener("click", {
            populateSubgridsTable(tableData.sortedByDescending { it.mass })
        })
        appendElement("th") {appendText("% Total Mass")}.addEventListener("click", {
            populateSubgridsTable(tableData.sortedByDescending { it.mass })
        })
        appendElement("th") {appendText("PCU")}.addEventListener("click", {
            populateSubgridsTable(tableData.sortedByDescending { it.pcu })
        })
        appendElement("th") {appendText("% Total PCU")}.addEventListener("click", {
            populateSubgridsTable(tableData.sortedByDescending { it.pcu })
        })
    }
    for(row in tableData) {
        row.toHtml(subgridsTable)
    }

    (this.getElementById("results") as HTMLDivElement).addClass("vizzibull")
    div.style.maxHeight = div.scrollHeight.toString() + "px"
}

fun Document.populateComponentsTable(tableData:List<Pair<String, Int>>) {
    val div = this.getElementById("components_div") as HTMLDivElement
    val componentsTable = this.getElementById("components_table") as HTMLTableElement
    componentsTable.clear()

    //name?, blocks, mass, pcu, small or large grid, % total blocks, % total mass, % total PCU
    componentsTable.appendElement("tr") {
        //name, count, mass, pcu
        appendElement("th") {appendText("Component")}.addEventListener("click", {
            populateComponentsTable(tableData.sortedBy { it.first })
        })
        appendElement("th") {appendText("Total")}.addEventListener("click", {
            populateComponentsTable(tableData.sortedByDescending { it.second })
        })
    }
    for(row in tableData) {
        componentsTable.appendElement("tr") {
            componentsTable.appendElement("td") { appendText(row.first.asDynamic().toLocaleString() as String) }
            componentsTable.appendElement("td") { appendText(row.second.asDynamic().toLocaleString() as String) }
        }
    }

    (this.getElementById("results") as HTMLDivElement).addClass("vizzibull")
    div.style.maxHeight = div.scrollHeight.toString() + "px"
}

fun Document.resetPage() {
    getElementById("unfound_blocks")?.removeClass("vizzibull")
    getElementById("invalid_file")?.removeClass("vizzibull")
    getElementById("unfound_blocks_list")?.clear()
}
