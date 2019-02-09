package group.openalcoholics.cocktailparty.db.dao

import com.google.inject.Inject
import group.openalcoholics.cocktailparty.model.IngredientCategory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.assertEquals

class IngredientCategoryDaoTest @Inject constructor(private val jdbi: Jdbi) : BaseDaoTest<IngredientCategory> {

    override fun create(id: Int): IngredientCategory =
        IngredientCategory(id, "testName$id", "testDescription$id", false, "testLink$id")

    override fun modifiedVersions(entity: IngredientCategory) = sequenceOf(
        entity.copy(name = entity.name + "Mod"),
        entity.copy(description = entity.description + "Mod"),
        entity.copy(alcoholic = !entity.alcoholic),
        entity.copy(imageLink = entity.imageLink + "Mod"),
        entity.copy(imageLink = null)
    )

    override fun find(id: Int): IngredientCategory? = jdbi.withExtensionUnchecked(IngredientCategoryDao::class) {
        it.find(id)
    }

    override fun insert(entity: IngredientCategory): Int = jdbi.withExtensionUnchecked(IngredientCategoryDao::class) {
        it.insert(entity)
    }

    override fun update(entity: IngredientCategory) = jdbi.useExtensionUnchecked(IngredientCategoryDao::class) {
        it.update(entity)
    }

    override fun delete(id: Int) = jdbi.useExtensionUnchecked(IngredientCategoryDao::class) {
        it.delete(id)
    }

    @TestFactory
    fun searchKnown(): Stream<DynamicTest> = sequenceOf(
        "" to 6, "rum" to 2)
        .map { (query, count) ->
            DynamicTest.dynamicTest("""Search for "$query"""") {
                val result = jdbi.withExtensionUnchecked(IngredientCategoryDao::class) {
                    it.search(query, 40, 0)
                }
                assertEquals(count, result.size)
            }
        }.asStream()

    @TestFactory
    fun searchUnknown(): Stream<DynamicTest> = sequenceOf("*", "moin", "l")
        .map { query ->
            DynamicTest.dynamicTest("""Search for "$query"""") {
                val result = jdbi.withExtensionUnchecked(IngredientCategoryDao::class) {
                    it.search(query, 40, 0)
                }
                assertEquals(emptyList(), result)
            }
        }.asStream()
}
