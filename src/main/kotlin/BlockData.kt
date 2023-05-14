/**Block has humanName: String, PCU: Int, and mass: Int? (because we might not have calculated it yet)*/
data class BlockData(
    val humanName: String,
    val pcu: Int,
    val mass: Int? = null
)