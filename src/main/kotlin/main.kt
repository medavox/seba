import org.w3c.dom.parsing.DOMParser
import kotlinx.browser.document
import kotlinx.dom.*
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.files.File
import org.w3c.files.FileReader

private var blueprintXmlDoc:XMLDocument? = null
/**values are negative for consumption, positive for generation*/
private val domParser: DOMParser = DOMParser()
private val allBlockDefs: MutableList<Element> = mutableListOf()

//TODO: power consumption

// in the supplied blueprint file bp.sbc:
fun processBlueprint(blueprint: XMLDocument) {
    //in Definitions > ShipBlueprint > CubeGrids:
    val cubeGrids: Element = blueprint.querySelector("Definitions > ShipBlueprints > ShipBlueprint > CubeGrids")
        ?: throw Exception("couldn't find CubeGrids element!")
    println("number of grids in blueprint:"+cubeGrids.childNodes.length)
    val mainGrid:Element = cubeGrids.firstElementChild ?: throw Exception("couldn't get first (presumably main) grid!")
    val mainGridBlocks:List<Element> = mainGrid.firstChildElementWithTag("CubeBlocks").children.asList()
    println("number of blocks in main grid:"+mainGridBlocks.size)
    //right, now let's get to work
    for(blockElement in mainGridBlocks) {
        //get human name
        val name = blockElement.firstChildElementWithTag("SubtypeName").textContent ?: throw Exception("block has no subtype!")

//        println("block data for $name: ${getBlockDataFor(name)}")
        getBlockDataFor(name)


        //                    in all of the CubeBlocks*.sbc files:
//                        in Definitions > CubeBlocks:
//                            find the <Definition> where Id > SubtypeId matches text of SubtypeName
//                            2. get the PCU value
//                                return the text of the <PCU> tag for that <Definition>
//                            3. get the mass
//                                in the <Components> tag of this <Definition>:
//                                    create a map from each <Component> tag:
//                                        Subtype to Count
//                                            (subtypes can appear more than once, so use a counting map)
//                                    in the Components.sbc file:
//                                        in Definitions > Components:
//                                            find the <Component> where Id > SubtypeId matches text of SubtypeName from map
//                                            in that <Component>:
//                                                return the text of the <Mass> tag
    }
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

private fun getBlockDataFor(subtype: String): Element? /*<Definition>*/ {
    return allBlockDefs.firstOrNull { blockDef ->
        blockDef.childElementsWithTag("SubtypeId").first().textContent == subtype
    }
}

//in the supplied blueprint file bp.sbc:
//in Definitions > ShipBlueprint > CubeGrids:
// for each CubeGrid (these are the grids & subgrids of the blueprint):
//    in CubeBlocks:
//        for each child:
//            take the text of <SubtypeName>
//                in all of the CubeBlocks*.sbc files:
//                    in Definitions > CubeBlocks:
//                        find the <Definition> where Id > SubtypeId matches text of SubtypeName
//                            1. get the human-friendly name:
//                                search for the DisplayName in the name attribute of every <data> tag,
//                                and return the text property of the child <value> tag
//                            2. get the PCU value
//                                return the text of the <PCU> tag for that <Definition>
//                            3. get the mass
//                                in the <Components> tag of this <Definition>:
//                                    create a map from each <Component> tag:
//                                        Subtype to Count
//                                            (subtypes can appear more than once, so use a counting map)
//                                    in the Components.sbc file:
//                                        in Definitions > Components:
//                                            find the <Component> where Id > SubtypeId matches text of SubtypeName from map
//                                            in that <Component>:
//                                                return the text of the <Mass> tag
//
//
// cache the retrieved human name in a map afterwards
// subtypeId: String to Block
//
// block masses and HP are calculated from their constituent parts, listed in Content/Data/Components.sbc



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
    //todo: find the best-matching recipe out of those returned
    // probably using levenshtein distance or similar
    val blueprintFileInput = document.getElementById("blueprint_file_input") as HTMLInputElement
    println("blueprint file input: $blueprintFileInput")

    //once the user provides a recipes.xml,
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
/*    if(search.value.length < 2) {
        return
    }*/
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

private fun Attr.asString() :String {
    return "$name=\"$value\""
}