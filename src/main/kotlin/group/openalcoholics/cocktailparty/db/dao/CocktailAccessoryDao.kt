package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.CocktailAccessory
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface CocktailAccessoryDao : SqlObject, BaseDao<CocktailAccessory> {
    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE cocktail_id = :cocktailId
    """)
    fun dropAccessories(cocktailId: Int)

    @SqlUpdate("""
        INSERT INTO $TABLE_NAME (
            cocktail_id,
            accessory_id,
            pieces)
        VALUES(
            :cocktailId,
            :accessoryId,
            :pieces)
    """)
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
    }
}