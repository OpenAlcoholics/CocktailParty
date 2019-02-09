package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.model.Cocktail
import group.openalcoholics.cocktailparty.model.CocktailAccessoryCategory
import group.openalcoholics.cocktailparty.model.CocktailIngredientCategory
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.sqlobject.SqlObject
import java.util.LinkedHashMap

interface CocktailDao : SqlObject, BaseDao<Cocktail> {
    override fun find(id: Int): Cocktail? = handle
        .createQuery("""
                SELECT $LOCAL_HEAD,
                    ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                    ${GlassDao.head("${GlassDao.TABLE_NAME}.")},
                    ${RecipeDao.head("${RecipeDao.TABLE_NAME}.")},
                    ${CocktailAccessoryCategoryDao.head(
            "${CocktailAccessoryCategoryDao.TABLE_NAME}.")}
                FROM (SELECT *
                    FROM $TABLE_NAME
                    WHERE $TABLE_NAME.id = :id
                ) AS $TABLE_NAME
                INNER JOIN ${CocktailCategoryDao.TABLE_NAME}
                    ON ${CocktailCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
                INNER JOIN ${GlassDao.TABLE_NAME}
                    ON ${GlassDao.TABLE_NAME}.id = $TABLE_NAME.glass_id
                LEFT JOIN ${RecipeDao.TABLE_NAME}
                    ON ${RecipeDao.TABLE_NAME}.cocktail_id = $TABLE_NAME.id
                LEFT JOIN ${CocktailAccessoryCategoryDao.TABLE_NAME}
                    ON ${CocktailAccessoryCategoryDao.TABLE_NAME}.cocktail_id = $TABLE_NAME.id
            """.trimIndent())
        .bind("id", id)
        .registerRowMapper(CocktailIngredientCategory::class.java,
            KotlinMapper(CocktailIngredientCategory::class.java,
                "${RecipeDao.TABLE_NAME}."))
        .registerRowMapper(CocktailAccessoryCategory::class.java,
            KotlinMapper(CocktailAccessoryCategory::class.java,
                "${CocktailAccessoryCategoryDao.TABLE_NAME}."))
        .reduceRows(null) { mainTail: Cocktail?, r ->
            val cocktail = mainTail ?: r.getRow(Cocktail::class.java)
            if (r.getColumn("${RecipeDao.TABLE_NAME}.cocktail_id",
                    Integer::class.java) != null) {
                val cocktailIngredient = r.getRow(CocktailIngredientCategory::class.java)
                cocktail.flatIngredientCategories.add(cocktailIngredient)
            }
            if (r.getColumn("${CocktailAccessoryCategoryDao.TABLE_NAME}.cocktail_id",
                    Integer::class.java) != null) {
                val cocktailAccessory = r.getRow(CocktailAccessoryCategory::class.java)
                cocktail.mutableAccessoryCategories.add(cocktailAccessory)
            }

            cocktail
        }
        ?.transferFromMutable()

    /**
     * Search for cocktails by a search query and/or a cocktail category.
     *
     * The columns that are included in the search by query remain unspecified.
     *
     * If no arguments are given, all cocktails are returned.
     *
     * @param query a search query
     * @param category a cocktail category ID
     * @param limit maximum result size
     * @param offset result offset
     * @return a list of matching cocktails
     */
    fun search(query: String?, category: Int?, limit: Int, offset: Int): List<Cocktail> = handle
        .createQuery("""
            SELECT $LOCAL_HEAD,
                ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                ${GlassDao.head("${GlassDao.TABLE_NAME}.")},
                ${RecipeDao.head("${RecipeDao.TABLE_NAME}.")},
                ${CocktailAccessoryCategoryDao.head("${CocktailAccessoryCategoryDao.TABLE_NAME}.")}
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
            LEFT JOIN ${RecipeDao.TABLE_NAME}
                ON ${RecipeDao.TABLE_NAME}.cocktail_id = $TABLE_NAME.id
            LEFT JOIN ${CocktailAccessoryCategoryDao.TABLE_NAME}
                ON ${CocktailAccessoryCategoryDao.TABLE_NAME}.cocktail_id = $TABLE_NAME.id
            LIMIT $limit
            OFFSET $offset
        """).apply {
            if (query != null) bind("q", query)
            if (category != null) bind("category", category)
        }
        .registerRowMapper(CocktailIngredientCategory::class.java,
            KotlinMapper(CocktailIngredientCategory::class.java,
                "${RecipeDao.TABLE_NAME}."))
        .registerRowMapper(CocktailAccessoryCategory::class.java,
            KotlinMapper(CocktailAccessoryCategory::class.java,
                "${CocktailAccessoryCategoryDao.TABLE_NAME}."))
        .reduceRows(LinkedHashMap<Int, Cocktail>()) { map, r ->
            val cocktail = map.computeIfAbsent(r.getColumn("id", Integer::class.java).toInt()) {
                val cocktail = r.getRow(Cocktail::class.java)
                cocktail
            }

            if (r.getColumn("${RecipeDao.TABLE_NAME}.cocktail_id",
                    Integer::class.java) != null) {
                val cocktailIngredient = r.getRow(CocktailIngredientCategory::class.java)
                cocktail.flatIngredientCategories.add(cocktailIngredient)
            }
            if (r.getColumn("${CocktailAccessoryCategoryDao.TABLE_NAME}.cocktail_id",
                    Integer::class.java) != null) {
                val cocktailAccessory = r.getRow(CocktailAccessoryCategory::class.java)
                cocktail.mutableAccessoryCategories.add(cocktailAccessory)
            }
            map
        }
        .values
        .map { it.transferFromMutable() }

    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "cocktail"
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
