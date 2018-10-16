package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.AccessoryCategory
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface AccessoryCategoryDao : SqlObject, BaseDao<AccessoryCategory> {
    @SqlQuery("""
        SELECT *
        FROM $TABLE_NAME
        WHERE id=:id
    """)
    override fun find(id: Int): AccessoryCategory?

    @GetGeneratedKeys("id")
    @SqlUpdate("""
        INSERT INTO $TABLE_NAME (name, description, image_link)
        VALUES (:entity.name, :entity.description, :entity.imageLink)
    """)
    override fun insert(entity: AccessoryCategory): Int

    @SqlUpdate("""
        UPDATE $TABLE_NAME
        SET name = :entity.name,
            description = :entity.description,
            image_link = :entity.imageLink
        WHERE id = :entity.id
    """)
    override fun update(entity: AccessoryCategory)

    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE id = :id
    """)
    override fun delete(id: Int)

    fun search(query: String): List<AccessoryCategory> = handle
        .createQuery("""
            SELECT * FROM $TABLE_NAME
            WHERE LOWER(name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))
        """)
        .bind("q", query)
        .mapTo(AccessoryCategory::class.java)
        .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "accessories_categories"
        override val tableName: String
            get() = TABLE_NAME
        override val columns = listOf("id", "name", "description", "image_link")
    }
}
