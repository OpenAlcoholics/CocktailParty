package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.models.Glass
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface GlassDao : SqlObject, BaseDao<Glass> {
    @SqlQuery("""
        SELECT *
        FROM $TABLE_NAME
        WHERE id=:id
    """)
    override fun find(id: Int): Glass?

    @GetGeneratedKeys("id")
    @SqlUpdate("""
        INSERT INTO $TABLE_NAME (name, estimated_size, image_link)
        VALUES (:entity.name, :entity.estimatedSize, :entity.imageLink)
    """)
    override fun insert(entity: Glass): Int

    @SqlUpdate("""
        UPDATE $TABLE_NAME
        SET name = :entity.name,
            estimated_size = :entity.estimatedSize,
            image_link = :entity.imageLink
        WHERE id = :entity.id
    """)
    override fun update(entity: Glass)

    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE id = :id
    """)
    override fun delete(id: Int)

    fun search(query: String): List<Glass> = handle
        .createQuery("""
            SELECT * FROM $TABLE_NAME
            WHERE LOWER(name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))
        """)
        .bind("q", query)
        .mapTo(Glass::class.java)
        .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "glasses"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf("id", "name", "estimated_size", "image_link")
    }
}
