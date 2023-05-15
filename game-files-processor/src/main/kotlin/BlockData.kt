/**Block has humanName: String, PCU: Int, and mass: Int? (because we might not have calculated it yet)*/
data class BlockData(
    val pcu: Int,
    val humanName: String? = null,
    val mass: Int? = null
)

//data we need from the cube block definitions:
//display name
//subtype id
//pcu
//  and mass is calculated