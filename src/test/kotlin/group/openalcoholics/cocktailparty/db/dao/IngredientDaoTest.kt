package group.openalcoholics.cocktailparty.db.dao

import com.google.inject.Inject
import group.openalcoholics.cocktailparty.model.Ingredient
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.assertEquals

class IngredientDaoTest @Inject constructor(private val jdbi: Jdbi) : BaseDaoTest<Ingredient> {

    private val generic = jdbi.withExtensionUnchecked(GenericIngredientDao::class) { dao ->
        (1..2).map { dao.find(it) }.map { it!! }.toList()
    }

    override fun create(id: Int): Ingredient = Ingredient(
        id,
        "name$id",
        100,
        generic[0],
        "link$id",
        "notes$id"
    )

    override fun modifiedVersions(entity: Ingredient): Sequence<Ingredient> = sequenceOf(
        entity.copy(name = entity.name + "Mod"),
        entity.copy(alcoholPercentage = entity.alcoholPercentage - 1),
        entity.copy(generic = generic[1]),
        entity.copy(imageLink = entity.imageLink + "Mod"),
        entity.copy(imageLink = null),
        entity.copy(notes = entity.notes + "Mod")
    )

    override fun find(id: Int): Ingredient? = jdbi.withExtensionUnchecked(IngredientDao::class) {
        it.find(id)
    }

    override fun insert(entity: Ingredient): Int = jdbi.withExtensionUnchecked(
        IngredientDao::class
    ) {
        it.insert(entity)
    }

    override fun update(entity: Ingredient) = jdbi.useExtensionUnchecked(IngredientDao::class) {
        it.update(entity)
    }

    override fun delete(id: Int) = jdbi.useExtensionUnchecked(IngredientDao::class) {
        it.delete(id)
    }

    private data class Search(val query: String?, val genericId: Int?, val expectedSize: Int)

    @TestFactory
    fun searchKnown(): Stream<DynamicTest> = sequenceOf(
        Search("", null, 5),
        Search(null, null, 5),
        Search("gin", null, 1),
        Search(null, 1, 2),
        Search(null, 2, 1)
    )
        .map { (query, genericId, expectedSize) ->
            DynamicTest.dynamicTest("""Search for "$query", genericId=$genericId""") {
                val result = jdbi.withExtensionUnchecked(IngredientDao::class) {
                    it.search(query, genericId, 40, 0)
                }
                assertEquals(expectedSize, result.size)
            }
        }.asStream()

    @TestFactory
    fun searchUnknown(): Stream<DynamicTest> = sequenceOf(
        "*" to null,
        "bricks" to null,
        null to 42
    )
        .map { (query, category) ->
            DynamicTest.dynamicTest("""Search for "$query"""") {
                val result = jdbi.withExtensionUnchecked(IngredientDao::class) {
                    it.search(query, category, 40, 0)
                }
                assertEquals(emptyList(), result)
            }
        }.asStream()
}
