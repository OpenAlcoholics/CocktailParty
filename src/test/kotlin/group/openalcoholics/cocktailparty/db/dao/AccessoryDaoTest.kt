package group.openalcoholics.cocktailparty.db.dao

import com.google.inject.Inject
import group.openalcoholics.cocktailparty.model.Accessory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.assertEquals

class AccessoryDaoTest @Inject constructor(private val jdbi: Jdbi) : BaseDaoTest<Accessory> {

    override fun create(id: Int): Accessory = Accessory(
        id = id,
        name = "name$id",
        description = "description$id",
        imageLink = "link$id"
    )

    override fun modifiedVersions(entity: Accessory): Sequence<Accessory> = sequenceOf(
        entity.copy(name = entity.name + "Mod"),
        entity.copy(description = entity.description + "Mod"),
        entity.copy(imageLink = entity.imageLink + "Mod"),
        entity.copy(imageLink = null)
    )

    override fun find(id: Int): Accessory? = jdbi.withExtensionUnchecked(AccessoryDao::class) {
        it.find(id)
    }

    override fun insert(entity: Accessory): Int = jdbi.withExtensionUnchecked(AccessoryDao::class) {
        it.insert(entity)
    }

    override fun update(entity: Accessory) = jdbi.useExtensionUnchecked(AccessoryDao::class) {
        it.update(entity)
    }

    override fun delete(id: Int) = jdbi.useExtensionUnchecked(AccessoryDao::class) {
        it.delete(id)
    }

    private data class Search(val query: String?, val expectedSize: Int)

    @TestFactory
    fun searchKnown(): Stream<DynamicTest> = sequenceOf(
        Search("", 2),
        Search(null, 2),
        Search("slice", 2),
        Search("cucumber", 1)
    )
        .map { (query, expectedSize) ->
            DynamicTest.dynamicTest("""Search for "$query"""") {
                val result = jdbi.withExtensionUnchecked(AccessoryDao::class) {
                    it.search(query, 40, 0)
                }
                assertEquals(expectedSize, result.size)
            }
        }.asStream()

    @TestFactory
    fun searchUnknown(): Stream<DynamicTest> = sequenceOf(
        "*",
        "bricks"
    )
        .map { query ->
            DynamicTest.dynamicTest("""Search for "$query"""") {
                val result = jdbi.withExtensionUnchecked(AccessoryDao::class) {
                    it.search(query, 40, 0)
                }
                assertEquals(emptyList(), result)
            }
        }.asStream()
}
