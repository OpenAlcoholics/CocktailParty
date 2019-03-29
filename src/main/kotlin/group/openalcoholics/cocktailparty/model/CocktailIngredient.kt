package group.openalcoholics.cocktailparty.model

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * The relation of an ingredient to a cocktail.
 *
 * @param genericIngredientId The ingredient generic ID.
 * @param share The share (in percent) of the ingredient in a cocktail.
 */
data class CocktailIngredient(
    val genericIngredientId: Int,
    val share: Int,
    @JsonIgnore
    val rank: Int? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CocktailIngredient

        if (genericIngredientId != other.genericIngredientId) return false
        if (share != other.share) return false

        return true
    }

    override fun hashCode(): Int {
        var result = genericIngredientId
        result = 31 * result + share
        return result
    }
}
