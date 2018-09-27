package group.openalcoholics.cocktailparty.db.dao

import com.google.inject.Inject
import group.openalcoholics.cocktailparty.models.AccessoryCategory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.assertEquals

class AccessoryCategoryDaoTest @Inject constructor(private val jdbi: Jdbi) :
    BaseDaoTest<AccessoryCategory> {

    override fun create(id: Int): AccessoryCategory =
        AccessoryCategory(id, "testName$id", "testDescription$id", "testLink$id")

    override fun modifiedVersions(entity: AccessoryCategory) = sequenceOf(
        entity.copy(name = entity.name + "Mod"),
        entity.copy(description = entity.description + "Mod"),
        entity.copy(imageLink = entity.imageLink + "Mod"),
        entity.copy(imageLink = null)
    )

    override fun find(id: Int): AccessoryCategory? = jdbi.withExtensionUnchecked(AccessoryCategoryDao::class) {
        it.find(id)
    }

    override fun insert(entity: AccessoryCategory): Int = jdbi.withExtensionUnchecked(AccessoryCategoryDao::class) {
        it.insert(entity)
    }

    override fun update(entity: AccessoryCategory) = jdbi.useExtensionUnchecked(AccessoryCategoryDao::class) {
        it.update(entity)
    }

    override fun delete(id: Int) = jdbi.useExtensionUnchecked(IngredientCategoryDao::class) {
        it.delete(id)
    }

    @TestFactory
    fun searchKnown(): Stream<DynamicTest> = sequenceOf(
        "" to 2, "cucumber" to 1, "CuMb" to 1)
        .map { (query, count) ->
            DynamicTest.dynamicTest("""Search for "$query"""") {
                val result = jdbi.withExtensionUnchecked(AccessoryCategoryDao::class) {
                    it.search(query)
                }
                assertEquals(count, result.size)
            }
        }.asStream()

    @TestFactory
    fun searchUnknown(): Stream<DynamicTest> = sequenceOf("*", "moin")
        .map { query ->
            DynamicTest.dynamicTest("""Search for "$query"""") {
                val result = jdbi.withExtensionUnchecked(AccessoryCategoryDao::class) {
                    it.search(query)
                }
                assertEquals(emptyList(), result)
            }
        }.asStream()
}
