data class RecipeData(
    val humanName: String,
    val displayName: String,
    val recipeName: String,
    val resultName: String,
    val recipeAmountsx1000: Map<String, Int>,
    val resultAmountx1000:Int,
    val productionTimeMs:Int
)  {
    override fun toString(): String {
        return "RecipeData(\n" +
                "\thumanName=\"$humanName\",\n" +
                "\tdisplayName=\"$displayName\",\n" +
                "\trecipeName=\"$recipeName\",\n" +
                "\tresultName=\"$resultName\",\n" +
                "\trecipeAmountsx1000=mapOf(" +recipeAmountsx1000.entries.joinToString { (key, value) -> "\"$key\" to $value"}+
                "),\n" +
                "\tresultAmountx1000=$resultAmountx1000,\n"+
                "\tproductionTimeMs=$productionTimeMs,\n"+
            ")"
    }
}