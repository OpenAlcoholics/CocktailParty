package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.IngredientCategory
import org.jdbi.v3.sqlobject.SqlObject

interface IngredientCategoryDao : SqlObject, BaseDao<IngredientCategory> {
    /**
     * Search for ingredient categories by a search query.
     *
     * The columns that are included in the search remain unspecified.
     *
     * @param query a search query
     * @return a list of matching categories
     */
    fun search(query: String): List<IngredientCategory> = handle
        .createQuery("""
            SELECT * FROM $TABLE_NAME
            WHERE LOWER(name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))
        """)
        .bind("q", query)
        .mapTo(IngredientCategory::class.java)
        .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "ingredient_category"
        override val tableName: String
            get() = TABLE_NAME
        override val columns = listOf("id", "name", "description", "is_alcoholic", "image_link")
    }
}
