import org.w3c.dom.HTMLTableElement

interface Htmlable {
    fun toHtml(table: HTMLTableElement)
}