package group.openalcoholics.cocktailparty.model

import org.jdbi.v3.core.mapper.reflect.ColumnName

/**
 * A generic ingredient.
 *
 * @param id The generic ingredient ID
 * @param name The name of the ingredient
 * @param description A description of the ingredient
 * @param isAlcoholic Whether ingredients of this type contain alcohol
 * @param imageLink A link to an image representing the ingredient
 */
data class GenericIngredient(
    val id: kotlin.Int,
    val name: kotlin.String,
    val description: String,
    val isAlcoholic: kotlin.Boolean,
    val imageLink: kotlin.String? = null
) : BaseModel<GenericIngredient> {

    override fun withId(id: Int): GenericIngredient {
        return copy(id = id)
    }
}

