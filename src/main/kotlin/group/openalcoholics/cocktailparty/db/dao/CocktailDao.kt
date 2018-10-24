package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.Cocktail
import group.openalcoholics.cocktailparty.model.CocktailAccessory
import group.openalcoholics.cocktailparty.model.CocktailIngredient
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.sqlobject.SqlObject
import java.util.*

interface CocktailDao : SqlObject, BaseDao<Cocktail> {
    override fun find(id: Int): Cocktail? = handle
        .createQuery("""
                SELECT $LOCAL_HEAD,
                    ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                    ${GlassDao.head("${GlassDao.TABLE_NAME}.")},
                    ${CocktailIngredientDao.head("${CocktailIngredientDao.TABLE_NAME}.")},
                    ${CocktailAccessoryDao.head("${CocktailAccessoryDao.TABLE_NAME}.")}
                FROM (SELECT *
                    FROM $TABLE_NAME
                    WHERE $TABLE_NAME.id = :id
                ) AS $TABLE_NAME
                INNER JOIN ${CocktailCategoryDao.TABLE_NAME}
                    ON ${CocktailCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
                INNER JOIN ${GlassDao.TABLE_NAME}
                    ON ${GlassDao.TABLE_NAME}.id = $TABLE_NAME.glass_id
                LEFT JOIN ${CocktailIngredientDao.TABLE_NAME}
                    ON ${CocktailIngredientDao.TABLE_NAME}.drink_id = $TABLE_NAME.id
                LEFT JOIN ${CocktailAccessoryDao.TABLE_NAME}
                    ON ${CocktailAccessoryDao.TABLE_NAME}.drink_id = $TABLE_NAME.id
            """.trimIndent())
        .bind("id", id)
        .registerRowMapper(CocktailIngredient::class.java,
            KotlinMapper(CocktailIngredient::class.java, "${CocktailIngredientDao.TABLE_NAME}."))
        .registerRowMapper(CocktailAccessory::class.java,
            KotlinMapper(CocktailAccessory::class.java, "${CocktailAccessoryDao.TABLE_NAME}."))
        .reduceRows(null) { mainTail: Cocktail?, r ->
            val cocktail = mainTail ?: r.getRow(Cocktail::class.java)
            if (r.getColumn("${CocktailIngredientDao.TABLE_NAME}.drink_id",
                    Integer::class.java) != null) {
                val cocktailIngredient = r.getRow(CocktailIngredient::class.java)
                cocktail.flatIngredients.add(cocktailIngredient)
            }
            if (r.getColumn("${CocktailAccessoryDao.TABLE_NAME}.drink_id",
                    Integer::class.java) != null) {
                val cocktailAccessory = r.getRow(CocktailAccessory::class.java)
                cocktail.mutableAccessories.add(cocktailAccessory)
            }

            cocktail
        }
        ?.transferFromMutable()

    fun search(query: String?, category: Int?): List<Cocktail> = handle
        .createQuery("""
            SELECT $LOCAL_HEAD,
                ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                ${GlassDao.head("${GlassDao.TABLE_NAME}.")},
                ${CocktailIngredientDao.head("${CocktailIngredientDao.TABLE_NAME}.")},
                ${CocktailAccessoryDao.head("${CocktailAccessoryDao.TABLE_NAME}.")}
            FROM (SELECT *
                FROM $TABLE_NAME
                WHERE (TRUE
                ${if (query == null) "" else "AND LOWER($TABLE_NAME.name) LIKE LOWER(CONCAT(\'%\', :q, \'%\'))"}
                ${if (category == null) "" else "AND $TABLE_NAME.category_id = :category"}
                )
            ) AS $TABLE_NAME
            INNER JOIN ${CocktailCategoryDao.TABLE_NAME}
                ON ${CocktailCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
            INNER JOIN ${GlassDao.TABLE_NAME}
                ON ${GlassDao.TABLE_NAME}.id = $TABLE_NAME.glass_id
            LEFT JOIN ${CocktailIngredientDao.TABLE_NAME}
                ON ${CocktailIngredientDao.TABLE_NAME}.drink_id = $TABLE_NAME.id
            LEFT JOIN ${CocktailAccessoryDao.TABLE_NAME}
                ON ${CocktailAccessoryDao.TABLE_NAME}.drink_id = $TABLE_NAME.id
        """).apply {
            if (query != null) bind("q", query)
            if (category != null) bind("category", category)
        }
        .registerRowMapper(CocktailIngredient::class.java,
            KotlinMapper(CocktailIngredient::class.java,
                "${CocktailIngredientDao.TABLE_NAME}."))
        .registerRowMapper(CocktailAccessory::class.java,
            KotlinMapper(CocktailAccessory::class.java,
                "${CocktailAccessoryDao.TABLE_NAME}."))
        .reduceRows(LinkedHashMap<Int, Cocktail>()) { map, r ->
            val cocktail = map.computeIfAbsent(r.getColumn("id", Integer::class.java).toInt()) {
                val cocktail = r.getRow(Cocktail::class.java)
                cocktail
            }

            if (r.getColumn("${CocktailIngredientDao.TABLE_NAME}.drink_id",
                    Integer::class.java) != null) {
                val cocktailIngredient = r.getRow(CocktailIngredient::class.java)
                cocktail.flatIngredients.add(cocktailIngredient)
            }
            if (r.getColumn("${CocktailAccessoryDao.TABLE_NAME}.drink_id",
                    Integer::class.java) != null) {
                val cocktailAccessory = r.getRow(CocktailAccessory::class.java)
                cocktail.mutableAccessories.add(cocktailAccessory)
            }
            map
        }
        .values
        .map { it.transferFromMutable() }

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "drinks"
        override val tableName: String
            get() = TABLE_NAME
        override val columns: List<String> = listOf(
            "id",
            "name",
            "image_link",
            "description",
            "revision_date",
            "notes",
            "category_id",
            "glass_id"
        )
        val LOCAL_HEAD = head("")
    }
}
