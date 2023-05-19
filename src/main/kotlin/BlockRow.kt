import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import org.w3c.dom.HTMLTableElement

data class BlockRow(val name: String, val count: Int, val pcu: Int, val mass: Double) : TableRow {
    override fun toHtml(table: HTMLTableElement) {
        table.appendElement("tr") {
            //name, count, mass, pcu
            appendElement("td") {appendText(name)}
            appendElement("td") {appendText(count.toString())}
            appendElement("td") {appendText(mass.toString())}
            appendElement("td") {appendText(pcu.toString())}
        }
    }
}