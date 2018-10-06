package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.models.Accessory
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface AccessoryDao: SqlObject, BaseDao<Accessory> {
    override fun find(id: Int): Accessory? = handle
        .createQuery("""
            SELECT $LOCAL_HEAD,
                ${AccessoryCategoryDao.head("${AccessoryCategoryDao.TABLE_NAME}.")}
            FROM $TABLE_NAME
            INNER JOIN ${AccessoryCategoryDao.TABLE_NAME}
                ON ${AccessoryCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
            WHERE $TABLE_NAME.id = :id
            """.trimIndent())
        .bind("id", id)
        .mapTo(Accessory::class.java)
        .findFirst()
        .orElse(null)

    @GetGeneratedKeys("id")
    @SqlUpdate("""
        INSERT INTO $TABLE_NAME(name, description, image_link, category_id)
        VALUES(:entity.name, :entity.description, :entity.imageLink, :entity.category.id)
    """)
    override fun insert(entity: Accessory): Int

    @SqlUpdate("""
        UPDATE $TABLE_NAME
        SET name = :entity.name, description = :entity.description, image_link = :entity.imageLink,
            category_id = :entity.category.id
        WHERE id = :entity.id
    """)
    override fun update(entity: Accessory)

    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE id = :id
    """)
    override fun delete(id: Int)

    fun search(query: String?, category: Int?): List<Accessory> = handle
        .createQuery("""
            SELECT $LOCAL_HEAD,
                ${AccessoryCategoryDao.head("${AccessoryCategoryDao.TABLE_NAME}.")}
            FROM $TABLE_NAME
            INNER JOIN ${AccessoryCategoryDao.TABLE_NAME}
                ON ${AccessoryCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
            WHERE (TRUE
                ${if (query == null) "" else "AND LOWER($TABLE_NAME.name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))"}
                ${if (category == null) "" else "AND $TABLE_NAME.category_id = :category"}
            )
            """)
        .apply {
            if (query != null) bind("q", query)
            if (category != null) bind("category", category)
        }.mapTo(Accessory::class.java).list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "accessories"
        override val tableName: String
            get() = TABLE_NAME
        override val columns = listOf("id", "name", "description", "image_link", "category_id")
        val LOCAL_HEAD = head("")
    }
}
