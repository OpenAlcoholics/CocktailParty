package group.openalcoholics.cocktailparty.models

/**
 * The relation of an accessory to a cocktail.
 *
 * @param accessoryId The accessory ID.
 * @param pieces The suggested amount of pieces of this accessory in a cocktail.
 */
data class CocktailAccessory(
    val accessoryId: Int,
    val pieces: Int
)
