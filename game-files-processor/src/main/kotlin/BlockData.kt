data class BlockSize(val x: Int, val y:Int, val z:Int)

/**Data retrieved from a block's <Definition> element in one of the CubeBlock Files.*/
data class BlockData(
    val pcu: Int,
    val typeId: String,
    val subtypeId: String,
    val humanName: String,
    val components: Map<String, Int>,
    val mass: Double,
    val gridSize: Char,
    val blockSize: BlockSize?=null,
    val dlc: String? = null,
    val xsiType: String = "",
) {
    override fun toString(): String {
        return "BlockData(\n" +
            "\ttypeId=\"$typeId\",\n" +
            (if(xsiType.isNotEmpty()) "\txsiType=\"$xsiType\",\n" else "") +
            "\tsubtypeId=\"$subtypeId\",\n" +
            "\thumanName=\"$humanName\",\n" +
            "\tgridSize='$gridSize',\n" +
            (if(blockSize == null) "" else "\tblockSize=BlockSize(${blockSize.x}, ${blockSize.y}, ${blockSize.z}),\n" )+
            "\tmass=$mass,\n" +
            "\tpcu=$pcu,\n"+
            (if (dlc == null) "" else "\tdlc=\"$dlc\",\n")+
            "\tcomponents=mapOf(" +components.entries.joinToString { (key, value) -> "\"$key\" to $value"}+
            ")\n" +
            ")"
    }
}
