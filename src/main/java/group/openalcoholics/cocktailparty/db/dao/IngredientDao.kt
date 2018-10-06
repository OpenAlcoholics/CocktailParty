package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.models.Ingredient
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate

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

    @GetGeneratedKeys("id")
    @SqlUpdate("""
        INSERT INTO $TABLE_NAME(name, image_link, notes, alcohol_percentage, category_id)
        VALUES(:entity.name, :entity.imageLink, :entity.notes, :entity.alcoholPercentage, :entity.category.id)
    """)
    override fun insert(entity: Ingredient): Int

    @SqlUpdate("""
        UPDATE $TABLE_NAME
        SET name = :entity.name, image_link = :entity.imageLink, notes = :entity.notes,
            alcohol_percentage = :entity.alcoholPercentage, category_id = :entity.category.id
        WHERE id = :entity.id
    """)
    override fun update(entity: Ingredient)

    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE id = :id
    """)
    override fun delete(id: Int)

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
