/**Data retrieved from a block's <Definition> element in one of the CubeBlock Files.
 *
 * other data we don't have yet:
 * mass is calculated, and human name is another lookup*/
data class BlockData(
    val pcu: Int,
    val displayName: String,
    val subtypeId: String,
)
