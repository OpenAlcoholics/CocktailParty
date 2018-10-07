package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.IngredientCategory
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface IngredientCategoryDao : SqlObject, BaseDao<IngredientCategory> {
    @SqlQuery("""
        SELECT *
        FROM $TABLE_NAME
        WHERE id=:id
    """)
    override fun find(id: Int): IngredientCategory?

    @GetGeneratedKeys("id")
    @SqlUpdate("""
        INSERT INTO $TABLE_NAME (name, description, image_link, is_alcoholic)
        VALUES (:entity.name, :entity.description, :entity.imageLink, :entity.alcoholic)
    """)
    override fun insert(entity: IngredientCategory): Int

    @SqlUpdate("""
        UPDATE $TABLE_NAME
        SET name = :entity.name,
            description = :entity.description,
            image_link = :entity.imageLink,
            is_alcoholic = :entity.alcoholic
        WHERE id = :entity.id
    """)
    override fun update(entity: IngredientCategory)

    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE id = :id
    """)
    override fun delete(id: Int)

    fun search(query: String): List<IngredientCategory> = handle
        .createQuery("""
            SELECT * FROM $TABLE_NAME
            WHERE LOWER(name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))
        """)
        .bind("q", query)
        .mapTo(IngredientCategory::class.java)
        .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "ingredient_categories"
        override val tableName: String
            get() = TABLE_NAME
        override val columns = listOf("id", "name", "description", "is_alcoholic", "image_link")
    }
}
