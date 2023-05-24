/**Data retrieved from a block's <Definition> element in one of the CubeBlock Files.*/
data class BlockData(
    val pcu: Int,
    val typeId: String,
    val subtypeId: String,
    val humanName: String,
    val components: Map<String, Int>,
    val mass: Double,
    val size: Char,
    val xsiType: String = "",
    val powerStorageKw: Int = 0,
    val maxPowerOutputKw: Int = 0,
    val pwrInputIdleMinKw: Int = 0,
    val pwrInputActiveMaxKw: Int = 0,
) {
    override fun toString(): String {
        return "BlockData(\n" +
            "\ttypeId=\"$typeId\",\n" +
            (if(xsiType.isNotEmpty()) "\txsiType=\"$xsiType\",\n" else "") +
            (if(powerStorageKw != 0) "\tpowerStorageKw=$powerStorageKw,\n" else "") +
            (if(maxPowerOutputKw != 0) "\tmaxPowerOutputKw=$maxPowerOutputKw,\n" else "") +
            (if(pwrInputIdleMinKw != 0) "\tpwrInputIdleMinKw=$pwrInputIdleMinKw,\n" else "") +
            (if(pwrInputActiveMaxKw != 0) "\tpwrInputActiveMaxKw=$pwrInputActiveMaxKw,\n" else "") +
            "\tsubtypeId=\"$subtypeId\",\n" +
            "\thumanName=\"$humanName\",\n" +
            "\tsize='$size',\n" +
            "\tmass=$mass,\n" +
            "\tpcu=$pcu,\n"+
            "\tcomponents=mapOf(" +components.entries.joinToString { (key, value) -> "\"$key\" to $value"}+
            ")\n" +
            ")"
    }
}
