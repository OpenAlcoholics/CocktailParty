package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.CocktailCategory
import org.jdbi.v3.sqlobject.SqlObject

interface CocktailCategoryDao : SqlObject, BaseDao<CocktailCategory> {
    /**
     * Search for cocktail categories by a search query.
     *
     * The columns that are included in the search remain unspecified.
     *
     * @param query a search query
     * @param limit maximum result size
     * @param offset result offset
     * @return a list of matching cocktail categories
     */
    fun search(query: String, limit: Int, offset: Int): List<CocktailCategory> = handle
        .createQuery("""
            SELECT * FROM $TABLE_NAME
            WHERE LOWER(name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))
            LIMIT $limit
            OFFSET $offset
            """)
        .bind("q", query)
        .mapTo(CocktailCategory::class.java)
        .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "cocktail_category"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf("id", "name", "description", "image_link")
    }
}
