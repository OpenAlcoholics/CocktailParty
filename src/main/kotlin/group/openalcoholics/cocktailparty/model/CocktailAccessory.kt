package group.openalcoholics.cocktailparty.model

import org.jdbi.v3.core.mapper.reflect.ColumnName

/**
 * The relation of an accessory to a cocktail.
 *
 * @param accessoryId The accessory generic ID.
 * @param pieces The suggested amount of pieces of this accessory in a cocktail.
 */
data class CocktailAccessory(
    @ColumnName("accessory_id")
    val accessoryId: Int,
    val pieces: Int
)
