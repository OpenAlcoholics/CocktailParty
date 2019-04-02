package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.CocktailAccessory
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface CocktailAccessoryDao : SqlObject {
    fun getAccessories(cocktailId: Int): List<CocktailAccessory> = handle
        .createQuery(
            """
                SELECT $LOCAL_HEAD
                FROM $TABLE_NAME
                WHERE cocktail_id = :cocktailId
            """.trimIndent()
        )
        .bind("cocktailId", cocktailId)
        .mapTo<CocktailAccessory>()
        .list()

    @SqlUpdate(
        """
            DELETE FROM $TABLE_NAME
            WHERE cocktail_id = :cocktailId
        """
    )
    fun deleteAccessory(cocktailId: Int, accessoryId: Int)

    @SqlUpdate(
        """
            INSERT INTO $TABLE_NAME (
                cocktail_id,
                accessory_id,
                pieces)
            VALUES(
                :cocktailId,
                :accessoryId,
                :pieces)
            ON CONFLICT ON CONSTRAINT cocktail_accessory_pk DO UPDATE
                SET pieces = EXCLUDED.pieces
        """
    )
    fun addAccessory(cocktailId: Int, accessoryId: Int, pieces: Int)

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "cocktail_accessory"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf(
            "cocktail_id",
            "accessory_id",
            "pieces"
        )
        val LOCAL_HEAD = head("")
    }
}
