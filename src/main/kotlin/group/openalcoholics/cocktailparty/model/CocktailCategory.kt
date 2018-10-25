package group.openalcoholics.cocktailparty.model

/**
 * A cocktail category.
 * @param id The category ID
 * @param name The name of the category
 * @param description The description for the category
 * @param imageLink A link to an image representing the category
 */
data class CocktailCategory(
    val id: kotlin.Int,
    val name: kotlin.String,
    val description: kotlin.String,
    val imageLink: kotlin.String? = null
) : BaseModel<CocktailCategory> {

    override fun withId(id: Int): CocktailCategory {
        return copy(id = id)
    }
}

