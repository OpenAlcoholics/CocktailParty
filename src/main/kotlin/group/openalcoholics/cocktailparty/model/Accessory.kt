package group.openalcoholics.cocktailparty.model

import group.openalcoholics.cocktailparty.db.dao.AccessoryCategoryDao
import org.jdbi.v3.core.mapper.Nested

/**
 * A manually applied accessory for a cocktail.
 *
 * @param id The accessory ID
 * @param name The name of the accessory
 * @param description The description of the accessory
 * @param imageLink A link to an image of the accessory
 * @param category
 */
data class Accessory(
    val id: kotlin.Int,
    val name: kotlin.String,
    val description: String,
    val imageLink: kotlin.String? = null,
    @Nested("${AccessoryCategoryDao.TABLE_NAME}.")
    val category: AccessoryCategory
) : BaseModel<Accessory> {

    override fun withId(id: Int): Accessory {
        return copy(id = id)
    }
}
