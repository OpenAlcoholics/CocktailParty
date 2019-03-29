package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.Accessory
import org.jdbi.v3.sqlobject.SqlObject

interface AccessoryDao : SqlObject, BaseDao<Accessory> {
    override fun find(id: Int): Accessory? = handle
        .createQuery("""
            SELECT $LOCAL_HEAD
            FROM $TABLE_NAME
            WHERE $TABLE_NAME.id = :id
            """.trimIndent())
        .bind("id", id)
        .mapTo(Accessory::class.java)
        .findFirst()
        .orElse(null)

    /**
     * Search for accessories by a search query and/or an accessory generic.
     *
     * The columns that are included in the search by query remain unspecified.
     *
     * If no arguments are given, all accessories are returned.
     *
     * @param query a search query
     * @param limit maximum result size
     * @param offset result offset
     * @return a list of matching accessories
     */
    fun search(query: String?, limit: Int, offset: Int): List<Accessory> = handle
        .createQuery("""
            SELECT $LOCAL_HEAD
            FROM $TABLE_NAME
            WHERE (TRUE
                ${if (query == null) "" else "AND LOWER($TABLE_NAME.name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))"}
            )
            LIMIT $limit
            OFFSET $offset
            """)
        .apply {
            if (query != null) bind("q", query)
        }.mapTo(Accessory::class.java).list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "accessory"
        override val tableName: String
            get() = TABLE_NAME
        override val columns = listOf("id", "name", "description", "image_link")
        val LOCAL_HEAD = head("")
    }
}
