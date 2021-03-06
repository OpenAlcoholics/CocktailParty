package group.openalcoholics.cocktailparty.model

import org.jdbi.v3.core.mapper.reflect.ColumnName

/**
 * An ingredient category.
 *
 * @param id The category ID
 * @param name The name of the category
 * @param description A description of the category
 * @param alcoholic Whether ingredients of this category contain alcohol
 * @param imageLink A link to an image representing the category
 */
data class IngredientCategory(
    val id: kotlin.Int,
    val name: kotlin.String,
    val description: String,
    @ColumnName("is_alcoholic")
    val alcoholic: kotlin.Boolean,
    val imageLink: kotlin.String? = null
) : BaseModel<IngredientCategory> {

    override fun withId(id: Int): IngredientCategory {
        return copy(id = id)
    }
}

