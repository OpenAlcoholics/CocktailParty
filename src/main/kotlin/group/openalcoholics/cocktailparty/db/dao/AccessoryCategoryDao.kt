package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.AccessoryCategory
import org.jdbi.v3.sqlobject.SqlObject

interface AccessoryCategoryDao : SqlObject, BaseDao<AccessoryCategory> {
    /**
     * Search for accessory categories by a search query.
     *
     * The columns that are included in the search remain unspecified.
     *
     * @param query a search query
     * @param limit maximum result size
     * @param offset result offset
     * @return a list of matching accessory categories
     */
    fun search(query: String?, limit: Int, offset: Int): List<AccessoryCategory> = handle
        .createQuery("""
            SELECT * FROM $TABLE_NAME
            ${if (query == null) "" else "WHERE LOWER(name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))"}
            LIMIT $limit
            OFFSET $offset
        """)
        .apply { if (query != null) bind("q", query) }
        .mapTo(AccessoryCategory::class.java)
        .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "accessory_category"
        override val tableName: String
            get() = TABLE_NAME
        override val columns = listOf("id", "name", "description", "image_link")
    }
}
