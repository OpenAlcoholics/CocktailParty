package group.openalcoholics.cocktailparty.models

import org.jdbi.v3.core.mapper.reflect.ColumnName

/**
 * The relation of an accessory to a cocktail.
 *
 * @param accessoryId The accessory ID.
 * @param pieces The suggested amount of pieces of this accessory in a cocktail.
 */
data class CocktailAccessory(
    @ColumnName("accessories_id")
    val accessoryId: Int,
    val pieces: Int
)
