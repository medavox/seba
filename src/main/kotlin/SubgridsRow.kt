import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import org.w3c.dom.HTMLTableElement

data class SubgridsRow(val name: String, val gridSize: GridSize,
                       val blocks: Int, val percentTotalBlocks:String,
                       val mass : Double, val percentTotalMass: String,
                       val pcu: Int, val percentTotalPcu: String
) : Htmlable {
    override fun toHtml(table: HTMLTableElement) {
        table.appendElement("tr") {

            appendElement("td") {appendText(name)}
            appendElement("td") {appendText(gridSize.asDynamic().toLocaleString() as String)}
            appendElement("td") {appendText(blocks.asDynamic().toLocaleString() as String)}
            appendElement("td") {appendText(percentTotalBlocks.asDynamic().toLocaleString() as String)}
            appendElement("td") {appendText(mass.asDynamic().toLocaleString() as String)}
            appendElement("td") {appendText(percentTotalMass.asDynamic().toLocaleString() as String)}
            appendElement("td") {appendText(pcu.asDynamic().toLocaleString() as String)}
            appendElement("td") {appendText(percentTotalPcu.asDynamic().toLocaleString() as String)}
        }
    }
}