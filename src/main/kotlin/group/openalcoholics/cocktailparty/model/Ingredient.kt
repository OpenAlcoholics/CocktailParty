package group.openalcoholics.cocktailparty.model

import group.openalcoholics.cocktailparty.db.dao.GenericIngredientDao
import org.jdbi.v3.core.mapper.Nested

/**
 * A liquid ingredient of a cocktail.
 * @param id The ingredient ID
 * @param name The name of the ingredient
 * @param imageLink A link to an image of the ingredient
 * @param alcoholPercentage The percentage of alcohol content in the ingredient
 * @param generic
 * @param notes Arbitrary notes on the ingredient
 */
data class Ingredient(
    val id: kotlin.Int,
    val name: kotlin.String,
    val alcoholPercentage: kotlin.Int,
    @Nested("${GenericIngredientDao.TABLE_NAME}.")
    val generic: GenericIngredient,
    val imageLink: kotlin.String? = null,
    val notes: kotlin.String? = null
) : BaseModel<Ingredient> {

    override fun withId(id: Int): Ingredient {
        return copy(id = id)
    }
}

