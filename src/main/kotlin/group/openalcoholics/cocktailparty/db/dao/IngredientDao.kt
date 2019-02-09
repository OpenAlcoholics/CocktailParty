package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.Ingredient
import org.jdbi.v3.sqlobject.SqlObject

interface IngredientDao : SqlObject, BaseDao<Ingredient> {

    override fun find(id: Int): Ingredient? = handle
        .createQuery("""
            SELECT $LOCAL_HEAD,
                ${IngredientCategoryDao.head("${IngredientCategoryDao.TABLE_NAME}.")}
            FROM $TABLE_NAME
            INNER JOIN ${IngredientCategoryDao.TABLE_NAME}
                ON ${IngredientCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
            WHERE $TABLE_NAME.id = :id
        """.trimIndent())
        .bind("id", id)
        .mapTo(Ingredient::class.java)
        .findFirst()
        .orElse(null)

    /**
     * Search for ingredients by a search query and/or an ingredient category.
     *
     * The columns that are included in the search by query remain unspecified.
     *
     * If no arguments are given, all ingredients are returned.
     *
     * @param query a search query
     * @param category a ingredient category ID
     * @param limit maximum result size
     * @param offset result offset
     * @return a list of matching ingredients
     */
    fun search(query: String?, category: Int?, limit: Int, offset: Int): List<Ingredient> = handle
        .createQuery("""
            SELECT $LOCAL_HEAD,
                ${IngredientCategoryDao.head("${IngredientCategoryDao.TABLE_NAME}.")}
            FROM $TABLE_NAME
            INNER JOIN ${IngredientCategoryDao.TABLE_NAME}
                ON ${IngredientCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
            WHERE (TRUE
                ${if (query == null) "" else "AND LOWER($TABLE_NAME.name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))"}
                ${if (category == null) "" else "AND $TABLE_NAME.category_id = :category"}
            )
            LIMIT $limit
            OFFSET $offset
        """)
        .apply {
            if (query != null) bind("q", query)
            if (category != null) bind("category", category)
        }.mapTo(Ingredient::class.java).list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "ingredient"
        override val tableName: String
            get() = TABLE_NAME
        override val columns = listOf(
            "id",
            "name",
            "image_link",
            "notes",
            "alcohol_percentage",
            "category_id")
        val LOCAL_HEAD = head("")
    }
}
