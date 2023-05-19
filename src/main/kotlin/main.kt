import org.w3c.dom.parsing.DOMParser
import kotlinx.browser.document
import kotlinx.dom.*
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.files.File
import org.w3c.files.FileReader

private var blueprintXmlDoc:XMLDocument? = null
private val domParser: DOMParser = DOMParser()

//TODO: power consumption:
//values are negative for consumption, positive for generation
// max power capacity
// time til drained when everything is running at full and no generators running
// can run indefinitely with everything on idle?
// can we meet max power demand? (is total max output >= max demand)
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
    for(blockElement in mainGridBlocks) {
        val subtype = blockElement.firstChildElementWithTag("SubtypeName").textContent ?: throw Exception("block has no subtype!")
        blockCounts[subtype] += 1
        /*val blockData = data.first {
            it.subtypeId == subtype
        }*/
    }
    return blockCounts
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

fun Document.recordIngredientsFor(name:String, quantity:Int) {
    val recipeCandidates:NodeList = this.querySelectorAll("recipe[name~=$name]")
    if(recipeCandidates.length == 0) {//no recipes matched
        //either the entered search term is bad, or the item IS uncraftable
        //add it to the uncraftables

        return
    }

    val filtered = recipeCandidates.asList().filter {
        it is Element && it.tagName == "recipe" }.map { it as Element }
    //println("possible candidates for crafting this item:"+filtered.size)
    //for now as a placeholder, select the first valid recipe in the list
    val item: Node = filtered[0]

    println("ingredients needed to craft $quantity ${(item as Element).attributes["name"]?.value}:")

    val ingredients:List<Element> = item.childNodes.asList().
        filter { it is Element && it.tagName == "ingredient" }.map { it as Element }

    for (ingredient in ingredients) {
        println("\t${ingredient.attributes["count"]?.value?.toInt()?.times(quantity)} " +
                "${ingredient.attributes["name"]?.value}")
        //get ingredient name and count needed
    }
    for (ingredient in ingredients) {
        this.recordIngredientsFor(ingredient.attributes["name"]!!.value,
                ingredient.attributes["count"]!!.value.toInt() * quantity)
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
            blueprintXmlDoc = domParser.parseFromString(loadedEvent.target.asDynamic().result as String,
                    "text/xml") as XMLDocument
            processBlueprint(blueprintXmlDoc!!)
            Unit //it's not redundant because kotlin/js is being doopid
        }
    })
    //onSubmitPressed()
}

/**called when the user presses the button to initiate the search*/
fun onSubmitPressed(it: MouseEvent) {
    //blueprintXmlDoc?.recordIngredientsFor(search.value, 1)
    val craftablesUi = document.getElementById("craftables") as HTMLTableElement
    val uncraftablesUi = document.getElementById("uncraftables") as HTMLTableElement
    uncraftablesUi.clear()
    craftablesUi.clear()
    uncraftablesUi.appendElement("tr") {
        //<tr><th>Name</th><th>Quantity</th></tr>
        appendElement("th") {appendText("Name")}
        appendElement("th") {appendText("Quantity")}
    }

    craftablesUi.appendElement("tr") {
        //<tr><th>Name</th><th>Quantity</th></tr>
        appendElement("th") {appendText("Name")}
        appendElement("th") {appendText("Quantity")}
    }

    (document.getElementById("results") as HTMLDivElement).addClass("vizzibull")
}

fun searchKeyInput(ke: KeyboardEvent) {

    //println("search value:"+search.value)
    val sugs = document.getElementById("suggestions") as HTMLDivElement
    sugs.addClass("vizzibull")
    if(sugs.hasChildNodes()) {
        sugs.clear()
    }
    blueprintXmlDoc.let { xml ->
        if(xml != null) {
/*            val cands = xml.getElementsByTagName("recipe").asList().filter { el ->
                el.attributes["name"]?.value?.toLowerCase()?.contains(search.value.toLowerCase()) ?: false
            }*/
//            print("candidates: ")
//            cands.forEach { println(it.asString()) }
            var i = 0
            //show no more than 6 suggestions
/*            while(i < min(6, cands.size)) {
                cands[i].getAttribute("name")?.let {recipe ->
                    sugs.appendElement("a") {
                        this.appendText(recipe)
                        this.addEventListener("click", {
                            search.value = recipe
                            sugs.removeClass("vizzibull")
                        })
                    }
                    i++
                }
            }*/
        }
    }

}

private fun Node.asString():String {
    return when(this){
        is Element -> {
            "Element \"$nodeName\"; "+this.attributes.asList().
            fold("") { acc, elem ->
                "$acc / ${elem.asString()}"
            }
        }
        is Text -> "Text node \""+this.wholeText+"\""
        else -> "Node \"$nodeName\"=\"$nodeValue\""
    }
}
