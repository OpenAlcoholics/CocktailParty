package group.openalcoholics.cocktailparty.db.dao

import org.jdbi.v3.sqlobject.customizer.Timestamped
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface BaseDao<T> {

    @UseClasspathSqlLocator
    @SqlQuery
    fun find(id: Int): T?

    @UseClasspathSqlLocator
    @GetGeneratedKeys("id")
    @Timestamped
    @SqlUpdate
    fun insert(entity: T): Int

    @UseClasspathSqlLocator
    @Timestamped
    @SqlUpdate
    fun update(entity: T)

    @UseClasspathSqlLocator
    @SqlUpdate
    fun delete(id: Int)
}

interface BaseDaoCompanion {
    val columns: List<String>
    val tableName: String
    fun head(prefix: String, tableQualifier: String = "$tableName."): String =
        columns.joinToString { """$tableQualifier$it AS "$prefix$it"""" }
}
