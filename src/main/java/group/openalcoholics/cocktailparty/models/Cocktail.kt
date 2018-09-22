/**
 * API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * OpenAPI spec version: 0.1.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package group.openalcoholics.cocktailparty.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import group.openalcoholics.cocktailparty.db.dao.CocktailCategoryDao
import group.openalcoholics.cocktailparty.db.dao.GlassDao
import org.jdbi.v3.core.mapper.Nested
import org.jdbi.v3.core.mapper.reflect.ColumnName
import java.time.Instant

/**
 * Information about a cocktail and its recipe.
 * @param id The cocktail ID
 * @param name The name of the cocktail
 * @param description A description of the cocktail
 * @param ingredients A list of ingredients. Items may be a single ingredient or a list of ingredients which can be poured simulatenously.
 * @param category
 * @param glass
 * @param imageLink A link to an image of the cocktail
 * @param revisionDate The time of the latest update to the recipe
 * @param notes Arbitrary notes on the cocktail
 */
data class Cocktail(
    /* The cocktail ID */
    val id: kotlin.Int,
    /* The name of the cocktail */
    val name: kotlin.String,
    /* A description of the cocktail */
    val description: kotlin.String,
    /* A list of ingredients. Items may be a single ingredient or a list of ingredients which can be poured simultaneously. */
    val ingredients: List<List<IngredientShare>> = emptyList(),
    @Nested("${CocktailCategoryDao.TABLE_NAME}.")
    val category: CocktailCategory,
    @Nested("${GlassDao.TABLE_NAME}.")
    val glass: Glass,
    /* A link to an image of the cocktail */
    val imageLink: kotlin.String? = null,
    /* The time of the latest update to the recipe */
    /* Arbitrary notes on the cocktail */
    val notes: kotlin.String? = null,
    val revisionDate: Long? = null,
    @JsonIgnore
    val flatIngredients: MutableList<IngredientShare> = mutableListOf()
) : BaseModel<Cocktail> {

    override fun withId(id: Int): Cocktail {
        return copy(id = id)
    }

    fun withSortedIngredients(): Cocktail {
        val parallelRanks = flatIngredients
            .groupBy { it.rank!! }
            .values
            .toList()

        return copy(ingredients = parallelRanks, flatIngredients = mutableListOf())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cocktail

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (ingredients != other.ingredients) return false
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
        result = 31 * result + (ingredients.hashCode())
        result = 31 * result + category.hashCode()
        result = 31 * result + glass.hashCode()
        result = 31 * result + (imageLink?.hashCode() ?: 0)
        result = 31 * result + (notes?.hashCode() ?: 0)
        return result
    }
}
