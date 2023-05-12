import org.w3c.dom.parsing.DOMParser
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.*
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.math.min

/**Keeps a count of the total amounts of each uncraftable thing we need,
 * for the requested recipe and all its intermediates*/
private val uncraftables = CountingMap<String>()

/**Keeps count of the items that need to be crafted.
 * Their insertion order is later used (in reverse)
 * to list the items in the order they can be crafted*/
private val toCraft = CountingMapWithOrder<String>()
private var lastSearchKeyPressTimeout:Int=0
private val search = document.getElementById("surch") as HTMLInputElement
private val button = document.getElementById("bouton") as HTMLButtonElement
private var recipesXmlDoc:Document? = null

/**Recursively finds ALL the materials needed to make this item, including sub-assemblies.
 * 1. Adds this item to the toCraft collection
 * 2. adds any items in its recipe that aren't found in recipes.xml to the uncraftables collection
 * 3. calls this method on any items that *were* found in recipes.xml
 *     //now for the meaty bit:
    for each ingredient required by this recipe,
    find that ingredient's own recipe, and its recipe's ingredients, and so on.
    recursively find all recipes and materials needed to make the chosen recipe.
    if any ingredient can't be found in the supplied recipes.xml,
    then we assume it's uncraftable/obtain-only.
    along with the obvious (like engines and electrical parts),
    this also includes materials you mine (such as ores).
    so we'll end up with a total list of materials to acquire,
    and a list of things to craft from them
    (preferably in order).
 *
 * @param name the item's name, as it appears (if it does) in `recipes.xml`
 */
fun Document.recordIngredientsFor(name:String, quantity:Int) {
    val recipeCandidates:NodeList = this.querySelectorAll("recipe[name~=$name]")
    if(recipeCandidates.length == 0) {//no recipes matched
        //either the entered search term is bad, or the item IS uncraftable
        //add it to the uncraftables

        uncraftables[name] += quantity
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
    if(ingredients.isEmpty()) {//recipes with no ingredients are also uncraftable,
        //rather than being craftable without any ingredients
        uncraftables[name] += quantity
        return
    }
    toCraft[name] += quantity
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
    val recipesFileInput = document.getElementById("recipes_file_input") as HTMLInputElement
    println("slekt: $recipesFileInput")

    //once the user provides a recipes.xml,
    // load its contents and enable the rest of the page UI
    recipesFileInput.addEventListener("change", { event ->
        val foyl:File = event.target.asDynamic().files[0] as File

        println("foil: ${foyl.name}; size: ${foyl.size}")
        val fr = FileReader()
        fr.readAsText(foyl)
        fr.onload = { loadedEvent ->
            recipesXmlDoc = DOMParser().parseFromString(loadedEvent.target.asDynamic().result as String,
                    "text/xml")
            //xmlDoc.visitItem("vehicle", 1)
            search.removeAttribute("disabled")
            button.removeAttribute("disabled")
            search.setAttribute("placeholder", "what would you like to craft?")
        }
    })

    //debounces the keyboard input: only calls the function that displays the search suggestions,
    //when 150ms have passed since the last keystroke
    search.onkeyup = {
        window.clearTimeout(lastSearchKeyPressTimeout)
        lastSearchKeyPressTimeout = window.setTimeout(::searchKeyInput, 150, it)
        Unit
    }

    button.onclick = ::onSubmitPressed
}

/**called when the user presses the button to initiate the search*/
fun onSubmitPressed(it:MouseEvent) {
    toCraft.clear()
    uncraftables.clear()
    recipesXmlDoc?.recordIngredientsFor(search.value, 1)
    val craftablesUi = document.getElementById("craftables") as HTMLTableElement
    val uncraftablesUi = document.getElementById("uncraftables") as HTMLTableElement
    uncraftablesUi.clear()
    craftablesUi.clear()
    uncraftablesUi.appendElement("tr") {
        //<tr><th>Name</th><th>Quantity</th></tr>
        appendElement("th") {appendText("Name")}
        appendElement("th") {appendText("Quantity")}
    }
    for(uncraftable in uncraftables.entries) {
        uncraftablesUi.appendElement("tr") {
            println("mouseEvent: $it")
            this.appendElement("td") {
                this.appendText(uncraftable.key)
            }
            this.appendElement("td") {
                this.appendText(uncraftable.value.toString())
            }
        }
    }
    craftablesUi.appendElement("tr") {
        //<tr><th>Name</th><th>Quantity</th></tr>
        appendElement("th") {appendText("Name")}
        appendElement("th") {appendText("Quantity")}
    }
    for(craftable in toCraft.elementsInInsertionOrder().reversed()) {
        craftablesUi.appendElement("tr") {
            this.appendElement("td") {
                this.appendText(craftable)
            }
            this.appendElement("td") {
                this.appendText(toCraft[craftable].toString())
            }
        }
    }
    (document.getElementById("results") as HTMLDivElement).addClass("vizzibull")
}

fun searchKeyInput(ke:KeyboardEvent) {
    if(search.value.length < 2) {
        return
    }
    println("search value:"+search.value)
    val sugs = document.getElementById("suggestions") as HTMLDivElement
    sugs.addClass("vizzibull")
    if(sugs.hasChildNodes()) {
        sugs.clear()
    }
    recipesXmlDoc.let { xml ->
        if(xml != null) {
            val cands = xml.getElementsByTagName("recipe").asList().filter { el ->
                el.attributes["name"]?.value?.toLowerCase()?.contains(search.value.toLowerCase()) ?: false
            }
//            print("candidates: ")
//            cands.forEach { println(it.asString()) }
            var i = 0
            //show no more than 6 suggestions
            while(i < min(6, cands.size)) {
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
            }
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