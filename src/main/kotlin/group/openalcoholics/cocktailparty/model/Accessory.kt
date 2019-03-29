package group.openalcoholics.cocktailparty.model

/**
 * A manually applied accessory for a cocktail.
 *
 * @param id The accessory ID
 * @param name The name of the accessory
 * @param description The description of the accessory
 * @param imageLink A link to an image of the accessory
 */
data class Accessory(
    val id: kotlin.Int,
    val name: kotlin.String,
    val description: String,
    val imageLink: kotlin.String? = null
) : BaseModel<Accessory> {

    override fun withId(id: Int): Accessory {
        return copy(id = id)
    }
}
