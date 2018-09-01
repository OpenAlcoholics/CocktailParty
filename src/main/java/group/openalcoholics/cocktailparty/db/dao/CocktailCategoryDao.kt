package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.models.CocktailCategory
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface CocktailCategoryDao : SqlObject, BaseDao<CocktailCategory> {
    @SqlQuery("""
        SELECT *
        FROM $TABLE_NAME
        WHERE id=:id
    """)
    override fun find(id: Int): CocktailCategory?

    @GetGeneratedKeys("id")
    @SqlUpdate("""
        INSERT INTO $TABLE_NAME (name, description, image_link)
        VALUES (:entity.name, :entity.description, :entity.imageLink)
    """)
    override fun insert(entity: CocktailCategory): Int

    @SqlUpdate("""
        UPDATE $TABLE_NAME
        SET name = :entity.name,
            description = :entity.description,
            image_link = :entity.imageLink
        WHERE id = :entity.id
    """)
    override fun update(entity: CocktailCategory)

    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE id = :id
    """)
    override fun delete(id: Int)

    fun search(query: String): List<CocktailCategory> = handle
            .createQuery("""
                SELECT * FROM $TABLE_NAME
                WHERE LOWER(name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))
            """)
            .bind("q", query)
            .mapTo(CocktailCategory::class.java)
            .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "categories"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf("id", "name", "description", "image_link")
    }
}
