package group.openalcoholics.cocktailparty.models

import com.fasterxml.jackson.annotation.JsonIgnore

data class IngredientShare(
    val ingredientId: Int,
    val share: Int,
    @JsonIgnore
    val rank: Int? = null) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IngredientShare

        if (ingredientId != other.ingredientId) return false
        if (share != other.share) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ingredientId
        result = 31 * result + share
        return result
    }
}
