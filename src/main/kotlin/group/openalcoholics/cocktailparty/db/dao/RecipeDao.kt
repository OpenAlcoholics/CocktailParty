package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.CocktailIngredientCategory
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface RecipeDao : SqlObject, BaseDao<CocktailIngredientCategory> {
    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE cocktail_id = :cocktailId
    """)
    fun dropIngredientCategories(cocktailId: Int)

    @SqlUpdate("""
        INSERT INTO $TABLE_NAME (
            cocktail_id,
            ingredient_category_id,
            share,
            rank)
        VALUES(
            :cocktailId,
            :ingredientCategoryId,
            :share,
            :rank)
    """)
    fun addIngredientCategory(cocktailId: Int, ingredientCategoryId: Int, share: Int, rank: Int)

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "recipe"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf(
            "cocktail_id",
            "ingredient_category_id",
            "share",
            "rank"
        )
    }
}
