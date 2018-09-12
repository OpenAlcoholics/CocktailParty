package group.openalcoholics.cocktailparty.db.dao

import com.google.inject.Inject
import group.openalcoholics.cocktailparty.models.Cocktail
import io.vertx.core.json.Json
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CocktailDaoTest @Inject constructor(private val jdbi: Jdbi) : BaseDaoTest<Cocktail> {
    private val ingredients = jdbi.withExtensionUnchecked(IngredientDao::class) { dao ->
        (1..3)
                .map { dao.find(it)!!.copy(share = it * 10, rank = it) }
                .toList()
    }
    private val categories = jdbi.withExtensionUnchecked(CocktailCategoryDao::class) { dao ->
        (1..2).map { dao.find(it) }.map { it!! }.toList()
    }
    private val glasses = jdbi.withExtensionUnchecked(GlassDao::class) { dao ->
        // TODO insert more mock glasses
        (1..1).map { dao.find(it) }.map { it!! }
    }

    override fun create(id: Int): Cocktail = Cocktail(
            id,
            "name$id",
            "desc$id",
            ingredients.subList(0, 2),
            categories.first(),
            glasses.first(),
            "link$id",
            "notes$id",
            0
    )

    override fun modifiedVersions(entity: Cocktail) = sequenceOf(
            entity.copy(name = entity.name + "Mod"),
            entity.copy(description = entity.description + "Mod"),
            entity.copy(category = categories[1]),
            entity.copy(imageLink = entity.imageLink + "Mod"),
            entity.copy(imageLink = null),
            entity.copy(notes = entity.notes + "Mod"),
            entity.copy(notes = null)
    )

    override fun find(id: Int): Cocktail? = jdbi.withExtensionUnchecked(CocktailDao::class) {
        it.find(id)
    }

    override fun insert(entity: Cocktail): Int = jdbi.withExtensionUnchecked(CocktailDao::class) {
        it.insert(entity).also { cocktailId ->
            for (ingredient in entity.flatIngredients()) {
                it.addIngredient(cocktailId, ingredient.id, ingredient.share!!, ingredient.rank!!)
            }
        }
    }

    override fun update(entity: Cocktail) = jdbi.useExtensionUnchecked(CocktailDao::class) {
        it.update(entity)
    }

    override fun delete(id: Int) = jdbi.useExtensionUnchecked(CocktailDao::class) {
        it.delete(id)
    }

    private data class Search(val query: String? = null,
                              val category: Int? = null,
                              val alcoholic: Boolean? = null,
                              val expectedSize: Int = 0)

    @TestFactory
    fun searchKnown(): Stream<DynamicTest> = sequenceOf(
            Search(expectedSize = 3),
            Search(alcoholic = true, expectedSize = 2),
            Search(query = "", expectedSize = 3),
            Search(query = "gin & tonic", expectedSize = 1),
            Search(query = "gin", expectedSize = 2),
            Search(query = "o", category = 1, expectedSize = 3),
            Search(alcoholic = false, expectedSize = 1))
            .map { (query, category, alcoholic, expectedSize) ->
                DynamicTest.dynamicTest("""Search for "$query" in category $category, alcoholic: $alcoholic""") {
                    val result = jdbi.withExtensionUnchecked(CocktailDao::class) {
                        it.search(query, category, alcoholic)
                    }
                    assertEquals(expectedSize, result.size, Json.encodePrettily(result))
                    for (cocktail in result) {
                        val ingredients = cocktail.flatIngredients().toList()
                        ingredients.forEach {
                            assertNotNull(it.rank)
                        }
                        assertEquals(ingredients.sortedBy { it.rank }, ingredients)
                    }
                }
            }.asStream()

    @TestFactory
    fun searchUnknown(): Stream<DynamicTest> = sequenceOf(
            Search("*"),
            Search("bricks"),
            Search(query = "o", category = 2),
            Search(query = "tonic", alcoholic = false))
            .map { (query, category, alcoholic) ->
                DynamicTest.dynamicTest("""Search for "$query, category=$category, alcoholic=$alcoholic"""") {
                    val result = jdbi.withExtensionUnchecked(CocktailDao::class) {
                        it.search(query, category, alcoholic)
                    }
                    assertEquals(emptyList(), result)
                }
            }.asStream()
}
