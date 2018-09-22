package group.openalcoholics.cocktailparty.models

import com.fasterxml.jackson.annotation.JsonIgnore

data class IngredientShare(
    val ingredientId: Int,
    val share: Int,
    @JsonIgnore
    val rank: Int? = null
)
