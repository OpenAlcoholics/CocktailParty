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

    fun search(query: String?, category: Int?): List<Ingredient> = handle
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
        """)
        .apply {
            if (query != null) bind("q", query)
            if (category != null) bind("category", category)
        }.mapTo(Ingredient::class.java).list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "ingredients"
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
