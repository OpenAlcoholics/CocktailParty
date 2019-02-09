package group.openalcoholics.cocktailparty.db.dao

import com.google.inject.Inject
import group.openalcoholics.cocktailparty.model.CocktailCategory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.assertEquals

class CocktailCategoryDaoTest @Inject constructor(private val jdbi: Jdbi) :
    BaseDaoTest<CocktailCategory> {

    override fun create(id: Int): CocktailCategory = CocktailCategory(
        id,
        "name$id",
        "desc$id",
        "image$id"
    )

    override fun modifiedVersions(entity: CocktailCategory) = sequenceOf(
        entity.copy(name = entity.name + "Mod"),
        entity.copy(description = entity.description + "Mod"),
        entity.copy(imageLink = entity.imageLink + "Mod"),
        entity.copy(imageLink = null)
    )

    override fun find(id: Int): CocktailCategory? = jdbi.withExtensionUnchecked(CocktailCategoryDao::class) {
        it.find(id)
    }

    override fun insert(entity: CocktailCategory): Int = jdbi.withExtensionUnchecked(CocktailCategoryDao::class) {
        it.insert(entity)
    }

    override fun update(entity: CocktailCategory) = jdbi.useExtensionUnchecked(CocktailCategoryDao::class) {
        it.update(entity)
    }

    override fun delete(id: Int) = jdbi.useExtensionUnchecked(CocktailCategoryDao::class) {
        it.find(id)
    }

    @TestFactory
    fun searchKnown(): Stream<DynamicTest> = sequenceOf(
        "" to 2, "high" to 1, "ghba" to 1, "ou" to 1, "s" to 2)
        .map { (query, count) ->
            DynamicTest.dynamicTest("""Search for "$query"""") {
                val result = jdbi.withExtensionUnchecked(CocktailCategoryDao::class) {
                    it.search(query, 40, 0)
                }
                assertEquals(count, result.size)
            }
        }.asStream()

    @TestFactory
    fun searchUnknown(): Stream<DynamicTest> = sequenceOf("*", "moin")
        .map { query ->
            DynamicTest.dynamicTest("""Search for "$query"""") {
                val result = jdbi.withExtensionUnchecked(CocktailCategoryDao::class) {
                    it.search(query, 40, 0)
                }
                assertEquals(emptyList(), result)
            }
        }.asStream()
}
