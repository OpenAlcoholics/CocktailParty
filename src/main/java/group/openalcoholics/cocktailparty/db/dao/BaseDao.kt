package group.openalcoholics.cocktailparty.db.dao

import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface BaseDao<T> {
    @SqlQuery
    fun find(id: Int): T?

    @SqlUpdate
    fun insert(entity: T): Int

    @SqlUpdate
    fun update(entity: T)

    @SqlUpdate
    fun delete(id: Int)
}

interface BaseDaoCompanion {
    val columns: List<String>
    val tableName: String
    fun head(prefix: String, tableQualifier: String = "$tableName."): String =
            columns.joinToString { """$tableQualifier$it AS "$prefix$it"""" }
}
