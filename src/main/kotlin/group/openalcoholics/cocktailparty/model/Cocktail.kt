package group.openalcoholics.cocktailparty.model

import com.fasterxml.jackson.annotation.JsonIgnore
import group.openalcoholics.cocktailparty.db.dao.CocktailCategoryDao
import group.openalcoholics.cocktailparty.db.dao.GlassDao
import org.jdbi.v3.core.mapper.Nested

/**
 * Information about a cocktail and its recipe.
 * @param id The cocktail ID
 * @param name The name of the cocktail
 * @param description A description of the cocktail
 * @param ingredients A list of ingredients.
 *     Items may be a single ingredient or a list of ingredients which can be poured simultaneously.
 * @param accessories A list of accessories that might be applied to the cocktail.
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
    val ingredients: List<List<CocktailIngredient>> = emptyList(),
    val accessories: Set<CocktailAccessory> = emptySet(),
    @Nested("${CocktailCategoryDao.TABLE_NAME}.")
    val category: CocktailCategory,
    @Nested("${GlassDao.TABLE_NAME}.")
    val glass: Glass,
    val imageLink: kotlin.String? = null,
    val notes: kotlin.String? = null,
    val revisionDate: Long? = null,
    @JsonIgnore
    val flatIngredients: MutableSet<CocktailIngredient> = mutableSetOf(),
    @JsonIgnore
    val mutableAccessories: MutableSet<CocktailAccessory> = mutableSetOf()
) : BaseModel<Cocktail> {

    override fun withId(id: Int): Cocktail {
        return copy(id = id)
    }

    /**
     * Transfers the data from [flatIngredients] and [mutableAccessories] to the
     * [ingredients] and [accessories] properties.
     *
     * @return a new Cocktail with the transferred values
     */
    fun transferFromMutable(): Cocktail {
        val sortedIngredients = flatIngredients
            .groupBy { it.rank!! }
            .values
            .toList()

        val accessories = mutableAccessories.toSet()

        return copy(ingredients = sortedIngredients, flatIngredients = mutableSetOf(),
            accessories = accessories, mutableAccessories = mutableSetOf())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cocktail

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (ingredients != other.ingredients) return false
        if (accessories != other.accessories) return false
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
        result = 31 * result + ingredients.hashCode()
        result = 31 * result + accessories.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + glass.hashCode()
        result = 31 * result + (imageLink?.hashCode() ?: 0)
        result = 31 * result + (notes?.hashCode() ?: 0)
        return result
    }
}
