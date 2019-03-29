package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.Cocktail
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.sqlobject.SqlObject

interface CocktailDao : SqlObject, BaseDao<Cocktail> {
    override fun find(id: Int): Cocktail? = handle
        .createQuery("""
                SELECT $LOCAL_HEAD,
                    ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                    ${GlassDao.head("${GlassDao.TABLE_NAME}.")}
                FROM (SELECT *
                    FROM $TABLE_NAME
                    WHERE $TABLE_NAME.id = :id
                ) AS $TABLE_NAME
                INNER JOIN ${CocktailCategoryDao.TABLE_NAME}
                    ON ${CocktailCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
                INNER JOIN ${GlassDao.TABLE_NAME}
                    ON ${GlassDao.TABLE_NAME}.id = $TABLE_NAME.glass_id
            """.trimIndent())
        .bind("id", id)
        .mapTo<Cocktail>()
        .findFirst()
        .orElse(null)

    /**
     * Search for cocktails by a search query and/or a cocktail generic.
     *
     * The columns that are included in the search by query remain unspecified.
     *
     * If no arguments are given, all cocktails are returned.
     *
     * @param query a search query
     * @param category a cocktail generic ID
     * @param limit maximum result size
     * @param offset result offset
     * @return a list of matching cocktails
     */
    fun search(query: String?, category: Int?, limit: Int, offset: Int): List<Cocktail> = handle
        .createQuery("""
            SELECT $LOCAL_HEAD,
                ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                ${GlassDao.head("${GlassDao.TABLE_NAME}.")}
            FROM (SELECT *
                FROM $TABLE_NAME
                WHERE (TRUE
                ${if (query == null) "" else "AND LOWER($TABLE_NAME.name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))"}
                ${if (category == null) "" else "AND $TABLE_NAME.category_id = :generic"}
                )
            ) AS $TABLE_NAME
            INNER JOIN ${CocktailCategoryDao.TABLE_NAME}
                ON ${CocktailCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
            INNER JOIN ${GlassDao.TABLE_NAME}
                ON ${GlassDao.TABLE_NAME}.id = $TABLE_NAME.glass_id
            LIMIT $limit
            OFFSET $offset
        """).apply {
            if (query != null) bind("q", query)
            if (category != null) bind("generic", category)
        }
        .mapTo<Cocktail>()
        .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "cocktail"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf(
            "id",
            "name",
            "image_link",
            "description",
            "revision_date",
            "notes",
            "category_id",
            "glass_id"
        )
        val LOCAL_HEAD = head("")
    }
}
