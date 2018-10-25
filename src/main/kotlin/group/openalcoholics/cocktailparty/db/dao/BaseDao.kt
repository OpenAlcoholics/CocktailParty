package group.openalcoholics.cocktailparty.db.dao

import org.jdbi.v3.sqlobject.customizer.Timestamped
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

/**
 * A DAO for CRUD operations on a specific entity type.
 *
 * Implementing classes should provide a companion object implementing [BaseDaoCompanion].
 *
 * @param T the entity type
 */
interface BaseDao<T> {

    /**
     * Finds a specific entity.
     * If the entity contains nested entities, they should be included as well.
     *
     * @param id the entity ID
     * @return the entity, or null
     */
    @UseClasspathSqlLocator
    @SqlQuery
    fun find(id: Int): T?

    /**
     * Inserts a new entity into the database.
     * Nested entities are not inserted by this method.
     *
     * @param entity an entity
     * @return the assigned entity ID
     */
    @UseClasspathSqlLocator
    @GetGeneratedKeys("id")
    @Timestamped
    @SqlUpdate
    fun insert(entity: T): Int

    /**
     * Updates an entity in the database.
     * Nested entities are not updated.
     *
     * @param entity an entity
     */
    @UseClasspathSqlLocator
    @Timestamped
    @SqlUpdate
    fun update(entity: T)

    /**
     * Deletes an entity from the database.
     * @param id the entity ID
     */
    @UseClasspathSqlLocator
    @SqlUpdate
    fun delete(id: Int)
}

/**
 * Base interface for companion objects for classes implementing [BaseDao].
 */
interface BaseDaoCompanion {

    /**
     * A list of column names of the associated database table.
     */
    val columns: List<String>
    /**
     * The table name for the entity in the associated database.
     */
    val tableName: String

    /**
     * Generates a head for SELECT statements in this table.
     *
     * The returned head has the form:
     *
     * `<tableQualifier>columnA AS "<prefix>columnA", <tableQualifier>columnB AS "<prefix>columnB"`
     *
     * @param prefix a prefix to prepend to the column names
     * @param tableQualifier the qualifier the column names currently have in the result set,
     * defaults to `TABLE_NAME.`
     * @return a select "head"
     */
    fun head(prefix: String, tableQualifier: String = "$tableName."): String =
        columns.joinToString { """$tableQualifier$it AS "$prefix$it"""" }
}
