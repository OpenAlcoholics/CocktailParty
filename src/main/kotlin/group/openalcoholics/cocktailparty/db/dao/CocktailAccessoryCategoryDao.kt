package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.CocktailAccessoryCategory
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface CocktailAccessoryCategoryDao : SqlObject, BaseDao<CocktailAccessoryCategory> {
    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE cocktail_id = :cocktailId
    """)
    fun dropAccessories(cocktailId: Int)

    @SqlUpdate("""
        INSERT INTO $TABLE_NAME (
            cocktail_id,
            accessory_category_id,
            pieces)
        VALUES(
            :cocktailId,
            :accessoryCategoryId,
            :pieces)
    """)
    fun addAccessory(cocktailId: Int, accessoryCategoryId: Int, pieces: Int)

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "cocktail_accessory"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf(
            "cocktail_id",
            "accessory_category_id",
            "pieces"
        )
    }
}
