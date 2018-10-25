package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.Accessory
import org.jdbi.v3.sqlobject.SqlObject

interface AccessoryDao : SqlObject, BaseDao<Accessory> {
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

    /**
     * Search for accessories by a search query and/or an accessory category.
     *
     * The columns that are included in the search by query remain unspecified.
     *
     * If no arguments are given, all accessories are returned.
     *
     * @param query a search query
     * @param category a accessory category ID
     * @return a list of matching accessories
     */
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
