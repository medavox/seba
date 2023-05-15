/**Data retrieved from a block's <Definition> element in one of the CubeBlock Files.
 *
 * other data we don't have yet:
 * mass is calculated, and human name is another lookup*/
data class BlockData(
    val pcu: Int,
    val displayName: String,
    val type: String,
    val subtypeId: String,
    val humanName: String,
    val components: Map<String, Int>,
    val mass: Double,
    val xsiType: String?,
) {
    override fun toString(): String {
        return "BlockData(pcu=$pcu,\n" +
            "\tdisplayName=\"$displayName\",\n" +
            "\ttype=\"$type\",\n" +
            "\tsubtypeId=\"$subtypeId\",\n" +
            "\thumanName=\"$humanName\",\n" +
            "\txsiType=\"$xsiType\",\n" +
            "\tmass=$mass,\n" +
            "\tcomponents=mapOf(\n" +
            "\t\t" +components.entries.fold("") {acc, (key, value) -> acc+"\"$key\" to $value, "}+
            "\t)\n" +
            ")"
    }
}
