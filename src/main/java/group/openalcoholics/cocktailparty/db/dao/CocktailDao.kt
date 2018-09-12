package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.models.Cocktail
import group.openalcoholics.cocktailparty.models.Ingredient
import group.openalcoholics.cocktailparty.models.IngredientComparator
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.Timestamped
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.time.OffsetDateTime
import java.util.*

interface CocktailDao : SqlObject, BaseDao<Cocktail> {
    override fun find(id: Int): Cocktail? = handle
            .createQuery("""
                SELECT $LOCAL_HEAD,
                    ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                    ${GlassDao.head("${GlassDao.TABLE_NAME}.")},
                    ${IngredientDao.head("${IngredientDao.TABLE_NAME}.")},
                    ${IngredientCategoryDao.head("${IngredientDao.TABLE_NAME}.${IngredientCategoryDao.TABLE_NAME}.")},
                    $INGREDIENT_RELATION_TABLE_NAME.share AS "$INGREDIENT_RELATION_TABLE_NAME.share",
                    $INGREDIENT_RELATION_TABLE_NAME.rank AS "$INGREDIENT_RELATION_TABLE_NAME.rank"
                FROM (SELECT *
                    FROM $TABLE_NAME
                    WHERE $TABLE_NAME.id = :id
                ) AS $TABLE_NAME
                INNER JOIN ${CocktailCategoryDao.TABLE_NAME}
                    ON ${CocktailCategoryDao.TABLE_NAME}.id = $TABLE_NAME.category_id
                INNER JOIN ${GlassDao.TABLE_NAME}
                    ON ${GlassDao.TABLE_NAME}.id = $TABLE_NAME.glass_id
                INNER JOIN $INGREDIENT_RELATION_TABLE_NAME
                    ON $INGREDIENT_RELATION_TABLE_NAME.drink_id = $TABLE_NAME.id
                INNER JOIN ${IngredientDao.TABLE_NAME}
                    ON ${IngredientDao.TABLE_NAME}.id = $INGREDIENT_RELATION_TABLE_NAME.ingredient_id
                INNER JOIN ${IngredientCategoryDao.TABLE_NAME}
                    ON ${IngredientCategoryDao.TABLE_NAME}.id = ${IngredientDao.TABLE_NAME}.category_id
            """.trimIndent())
            .bind("id", id)
            .registerRowMapper(Ingredient::class.java, KotlinMapper(Ingredient::class.java, "${IngredientDao.TABLE_NAME}."))
            .reduceRows(null) { mainTail: Cocktail?, r ->
                val cocktail = mainTail ?: r.getRow(Cocktail::class.java)
                val share = r.getColumn("$INGREDIENT_RELATION_TABLE_NAME.share", Integer::class.java).toInt()
                val rank = r.getColumn("$INGREDIENT_RELATION_TABLE_NAME.rank", Integer::class.java).toInt()
                val ingredient = r.getRow(Ingredient::class.java)
                        .copy(share = share, rank = rank)
                (cocktail.ingredients as MutableList<Ingredient>).add(ingredient)
                cocktail
            }
            ?.normalizeIngredients()

    private fun Cocktail.normalizeIngredients(): Cocktail = apply {
        @Suppress("UNCHECKED_CAST")
        (ingredients as MutableList<Ingredient>).sortBy { it.rank }
        val parallelRanks = flatIngredients()
                .groupBy { it.rank!! }
                .values.asSequence()
                .map {
                    @Suppress("IMPLICIT_CAST_TO_ANY")
                    if (it.size == 1) it.first() else it
                }
                .toList()

        val untypedIngredients = (ingredients as MutableList<Any>)
        untypedIngredients.clear()
        untypedIngredients.addAll(parallelRanks)
    }

    @GetGeneratedKeys("id")
    @Timestamped
    @SqlUpdate("""
        INSERT INTO $TABLE_NAME(name, image_link, description, revision_date, notes, category_id, glass_id)
        VALUES(:entity.name, :entity.imageLink, :entity.description, :now, :entity.notes, :entity.category.id, :entity.glass.id)
    """)
    override fun insert(entity: Cocktail): Int

    @SqlUpdate("""
        INSERT INTO $INGREDIENT_RELATION_TABLE_NAME(
            drink_id,
            ingredient_id,
            share,
            rank)
        VALUES(
            :cocktailId,
            :ingredientId,
            :share,
            :rank)
    """)
    fun addIngredient(cocktailId: Int, ingredientId: Int, share: Int, rank: Int)

    @SqlUpdate("""
        DELETE
        FROM $INGREDIENT_RELATION_TABLE_NAME
        WHERE drink_id = :cocktailId AND ingredient_id = :ingredientId
    """)
    fun removeIngredient(cocktailId: Int, ingredientId: Int)

    @SqlUpdate("""
        UPDATE $INGREDIENT_RELATION_TABLE_NAME
        SET rank = :rank
        WHERE drink_id = :cocktailId AND ingredient_id = :ingredientId
    """)
    fun updateIngredientRank(cocktailId: Int, ingredientId: Int, rank: Int)

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

    fun search(query: String?, category: Int?, alcoholic: Boolean?): List<Cocktail> = handle
            .createQuery("""
                SELECT $LOCAL_HEAD,
                    ${CocktailCategoryDao.head("${CocktailCategoryDao.TABLE_NAME}.")},
                    ${GlassDao.head("${GlassDao.TABLE_NAME}.")},
                    ${IngredientDao.head("${IngredientDao.TABLE_NAME}.")},
                    ${IngredientCategoryDao.head("${IngredientDao.TABLE_NAME}.${IngredientCategoryDao.TABLE_NAME}.")},
                    $INGREDIENT_RELATION_TABLE_NAME.share AS "$INGREDIENT_RELATION_TABLE_NAME.share",
                    $INGREDIENT_RELATION_TABLE_NAME.rank AS "$INGREDIENT_RELATION_TABLE_NAME.rank"
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
                INNER JOIN $INGREDIENT_RELATION_TABLE_NAME
                    ON $INGREDIENT_RELATION_TABLE_NAME.drink_id = $TABLE_NAME.id
                INNER JOIN ${IngredientDao.TABLE_NAME}
                    ON ${IngredientDao.TABLE_NAME}.id = $INGREDIENT_RELATION_TABLE_NAME.ingredient_id
                INNER JOIN ${IngredientCategoryDao.TABLE_NAME}
                    ON ${IngredientCategoryDao.TABLE_NAME}.id = ${IngredientDao.TABLE_NAME}.category_id
            """).apply {
                if (query != null) bind("q", query)
                if (category != null) bind("category", category)
            }
            .registerRowMapper(Ingredient::class.java, KotlinMapper(Ingredient::class.java, "${IngredientDao.TABLE_NAME}."))
            .reduceRows(LinkedHashMap<Int, Cocktail>()) { map, r ->
                val cocktail = map.computeIfAbsent(r.getColumn("id", Integer::class.java).toInt()) {
                    val cocktail = r.getRow(Cocktail::class.java)
                    cocktail
                }
                val share = r.getColumn("$INGREDIENT_RELATION_TABLE_NAME.share", Integer::class.java).toInt()
                val rank = r.getColumn("$INGREDIENT_RELATION_TABLE_NAME.rank", Integer::class.java).toInt()
                val ingredient = r.getRow(Ingredient::class.java)
                        .copy(share = share, rank = rank)
                (cocktail.ingredients as MutableList<Any>).add(ingredient)
                cocktail.normalizeIngredients()
                map
            }
            .values
            .asSequence()
            .onEach { cocktail ->
                (cocktail.ingredients as MutableList<Any>)
                        .sortedWith(IngredientComparator)
            }
            .let { cocktails ->
                if (alcoholic == null) cocktails
                else cocktails.filter { cocktail ->
                    cocktail.flatIngredients()
                            .map { it.category }
                            .filter { it.alcoholic }
                            .run {
                                if (alcoholic) any()
                                else none()
                            }
                }
            }
            .toList()


    companion object : BaseDaoCompanion {
        const val TABLE_NAME = "drinks"
        const val INGREDIENT_RELATION_TABLE_NAME = "drink_ingredients"
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
