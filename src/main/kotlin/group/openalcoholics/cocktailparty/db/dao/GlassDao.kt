package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.Glass
import org.jdbi.v3.sqlobject.SqlObject

interface GlassDao : SqlObject, BaseDao<Glass> {
    /**
     * Search for glasses by a search query.
     *
     * The columns that are included in the search remain unspecified.
     *
     * @param query a search query
     * @return a list of matching glasses
     */
    fun search(query: String?): List<Glass> = handle
        .createQuery("""
            SELECT * FROM $TABLE_NAME
            ${if (query == null) "" else "WHERE LOWER(name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))"}
        """)
        .apply { if (query != null) bind("q", query) }
        .mapTo(Glass::class.java)
        .list()

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "glass"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf("id", "name", "estimated_size", "image_link")
    }
}
