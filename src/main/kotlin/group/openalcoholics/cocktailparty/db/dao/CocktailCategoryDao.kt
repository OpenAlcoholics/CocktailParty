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
     * @return a list of matching cocktail categories
     */
    fun search(query: String): List<CocktailCategory> = handle
        .createQuery("""
                SELECT * FROM $TABLE_NAME
                WHERE LOWER(name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))
            """)
        .bind("q", query)
        .mapTo(CocktailCategory::class.java)
        .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "drink_categories"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf("id", "name", "description", "image_link")
    }
}
