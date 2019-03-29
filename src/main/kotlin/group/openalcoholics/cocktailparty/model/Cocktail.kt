package group.openalcoholics.cocktailparty.model

import group.openalcoholics.cocktailparty.db.dao.CocktailCategoryDao
import group.openalcoholics.cocktailparty.db.dao.GlassDao
import org.jdbi.v3.core.mapper.Nested

/**
 * Information about a cocktail and its recipe.
 * @param id The cocktail ID
 * @param name The name of the cocktail
 * @param description A description of the cocktail
 * @param category
 * @param glass
 * @param imageLink A link to an image of the cocktail
 * @param revisionDate The time of the latest update to the recipe
 * @param notes Arbitrary notes on the cocktail
 */
data class Cocktail(
    val id: kotlin.Int,
    val name: kotlin.String,
    val description: kotlin.String,
    @Nested("${CocktailCategoryDao.TABLE_NAME}.")
    val category: CocktailCategory,
    @Nested("${GlassDao.TABLE_NAME}.")
    val glass: Glass,
    val imageLink: kotlin.String? = null,
    val notes: kotlin.String? = null,
    val revisionDate: Long? = null
) : BaseModel<Cocktail> {

    override fun withId(id: Int): Cocktail {
        return copy(id = id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cocktail

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (category != other.category) return false
        if (glass != other.glass) return false
        if (imageLink != other.imageLink) return false
        if (notes != other.notes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + glass.hashCode()
        result = 31 * result + (imageLink?.hashCode() ?: 0)
        result = 31 * result + (notes?.hashCode() ?: 0)
        return result
    }
}
