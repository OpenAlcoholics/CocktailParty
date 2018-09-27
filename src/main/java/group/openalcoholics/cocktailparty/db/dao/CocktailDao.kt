package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.models.Cocktail
import group.openalcoholics.cocktailparty.models.IngredientShare
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.customizer.Timestamped
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface CocktailDao : SqlObject, BaseDao<Cocktail> {
    override fun find(id: Int): Cocktail? = handle
        .createQuery("""
                SELECT $LOCAL_HEAD,
                    ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                    ${GlassDao.head("${GlassDao.TABLE_NAME}.")},
                    ${IngredientShareDao.head("${IngredientShareDao.TABLE_NAME}.")}
                FROM (SELECT *
                    FROM $TABLE_NAME
                    WHERE $TABLE_NAME.id = :id
                ) AS $TABLE_NAME
                INNER JOIN ${CocktailCategoryDao.TABLE_NAME}
                    ON ${CocktailCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
                INNER JOIN ${GlassDao.TABLE_NAME}
                    ON ${GlassDao.TABLE_NAME}.id = $TABLE_NAME.glass_id
                INNER JOIN ${IngredientShareDao.TABLE_NAME}
                    ON ${IngredientShareDao.TABLE_NAME}.drink_id = $TABLE_NAME.id
            """.trimIndent())
        .bind("id", id)
        .registerRowMapper(IngredientShare::class.java,
            KotlinMapper(IngredientShare::class.java, "${IngredientShareDao.TABLE_NAME}."))
        .reduceRows(null) { mainTail: Cocktail?, r ->
            val cocktail = mainTail ?: r.getRow(Cocktail::class.java)
            val ingredientShare = r.getRow(IngredientShare::class.java)
            cocktail.flatIngredients.add(ingredientShare)
            cocktail
        }
        ?.transferFromMutable()

    @GetGeneratedKeys("id")
    @Timestamped
    @SqlUpdate("""
        INSERT INTO $TABLE_NAME(name, image_link, description, revision_date, notes, category_id, glass_id)
        VALUES(:entity.name, :entity.imageLink, :entity.description, :now, :entity.notes, :entity.category.id, :entity.glass.id)
    """)
    override fun insert(entity: Cocktail): Int

    @Timestamped
    @SqlUpdate("""
        UPDATE $TABLE_NAME
        SET name = :entity.name, image_link = :entity.imageLink, description = :entity.description,
            revision_date = :now, notes = :entity.notes, category_id = :entity.category.id,
            glass_id = :entity.glass.id
        WHERE id = :entity.id
    """)
    override fun update(entity: Cocktail)

    @SqlUpdate("""
        DELETE FROM $TABLE_NAME
        WHERE id = :id
    """)
    override fun delete(id: Int)

    fun search(query: String?, category: Int?): List<Cocktail> = handle
        .createQuery("""
                SELECT $LOCAL_HEAD,
                    ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                    ${GlassDao.head("${GlassDao.TABLE_NAME}.")},
                    ${IngredientShareDao.head("${IngredientShareDao.TABLE_NAME}.")}
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
                INNER JOIN ${IngredientShareDao.TABLE_NAME}
                    ON ${IngredientShareDao.TABLE_NAME}.drink_id = $TABLE_NAME.id
            """).apply {
            if (query != null) bind("q", query)
            if (category != null) bind("category", category)
        }
        .registerRowMapper(IngredientShare::class.java,
            KotlinMapper(IngredientShare::class.java,
                "${IngredientShareDao.TABLE_NAME}."))
        .reduceRows(LinkedHashMap<Int, Cocktail>()) { map, r ->
            val cocktail = map.computeIfAbsent(r.getColumn("id", Integer::class.java).toInt()) {
                val cocktail = r.getRow(Cocktail::class.java)
                cocktail
            }

            val ingredientShare = r.getRow(IngredientShare::class.java)
            cocktail.flatIngredients.add(ingredientShare)
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
