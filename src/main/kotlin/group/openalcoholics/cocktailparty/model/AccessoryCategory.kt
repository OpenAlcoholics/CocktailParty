package group.openalcoholics.cocktailparty.model

/**
 * An accessory category.
 *
 * @param id The category ID
 * @param name The name of the category
 * @param description A description of the category
 * @param imageLink A link to an image representing the category
 */
data class AccessoryCategory(
    val id: kotlin.Int,
    val name: kotlin.String,
    val description: String,
    val imageLink: kotlin.String? = null
) : BaseModel<AccessoryCategory> {

    override fun withId(id: Int): AccessoryCategory {
        return copy(id = id)
    }
}
