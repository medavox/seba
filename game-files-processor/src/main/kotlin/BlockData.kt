/**Data retrieved from a block's <Definition> element in one of the CubeBlock Files.*/
data class BlockData(
    val pcu: Int,
    val typeId: String,
    val subtypeId: String,
    val humanName: String,
    val components: Map<String, Int>,
    val mass: Double,
    val xsiType: String = "",
) {
    override fun toString(): String {
        return "BlockData(pcu=$pcu,\n" +
            "\ttypeId=\"$typeId\",\n" +
            "\tsubtypeId=\"$subtypeId\",\n" +
            "\thumanName=\"$humanName\",\n" +
                (if(xsiType.isNotEmpty()) "\txsiType=\"$xsiType\",\n" else "") +
            "\tmass=$mass,\n" +
            "\tcomponents=mapOf(" +components.entries.fold("") {acc, (key, value) -> acc+"\"$key\" to $value, "}+
            ")\n" +
            ")"
    }
}
