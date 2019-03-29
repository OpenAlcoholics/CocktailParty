package group.openalcoholics.cocktailparty.model

/**
 * A cocktail generic.
 * @param id The generic ID
 * @param name The name of the generic
 * @param description The description for the generic
 * @param imageLink A link to an image representing the generic
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

