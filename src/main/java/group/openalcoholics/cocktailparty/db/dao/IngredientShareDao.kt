package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.models.IngredientShare
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface IngredientShareDao : BaseDao<IngredientShare> {
    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE drink_id = :id
    """)
    fun dropIngredients(cocktailId: Int)

    @SqlUpdate("""
        INSERT INTO $TABLE_NAME (
            drink_id,
            ingredient_id,
            share,
            rank)
        VALUES(
            :cocktailId,
            :ingredientId,
            :share,
            :rank)
    """)
    fun addIngredient(cocktailId: Int, ingredientId: Int, share: Int, rank: Int)

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "drink_ingredients"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf(
            "drink_id",
            "ingredient_id",
            "share",
            "rank"
        )
    }
}
