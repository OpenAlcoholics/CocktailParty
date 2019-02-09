package group.openalcoholics.cocktailparty.model

import org.jdbi.v3.core.mapper.reflect.ColumnName

/**
 * The relation of an accessory to a cocktail.
 *
 * @param accessoryCategoryId The accessory category ID.
 * @param pieces The suggested amount of pieces of this accessory in a cocktail.
 */
data class CocktailAccessoryCategory(
    @ColumnName("accessory_category_id")
    val accessoryCategoryId: Int,
    val pieces: Int
)
