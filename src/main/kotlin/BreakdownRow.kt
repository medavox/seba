import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import org.w3c.dom.HTMLTableElement

data class BreakdownRow(val name: String, val count: Int, val pcu: Int, val mass: Double) : Htmlable {
    override fun toHtml(table: HTMLTableElement) {
        table.appendElement("tr") {
            //name, count, mass, pcu
            appendElement("td") {appendText(name)}
            appendElement("td") {appendText(count.asDynamic().toLocaleString() as String)}
            appendElement("td") {appendText(mass.asDynamic().toLocaleString() as String)}
            appendElement("td") {appendText(pcu.asDynamic().toLocaleString() as String)}
        }
    }
}