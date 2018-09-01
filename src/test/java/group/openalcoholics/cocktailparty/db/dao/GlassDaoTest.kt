package group.openalcoholics.cocktailparty.db.dao

import com.google.inject.Inject
import group.openalcoholics.cocktailparty.models.Glass
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.assertEquals

class GlassDaoTest @Inject constructor(private val jdbi: Jdbi) : BaseDaoTest<Glass> {
    override fun create(id: Int): Glass {
        return Glass(id, "testName$id", id * 100, "testLink$id")
    }

    override fun modifiedVersions(entity: Glass): Sequence<Glass> = sequenceOf(
            entity.copy(name = entity.name + "Mod"),
            entity.copy(estimatedSize = entity.estimatedSize + 1),
            entity.copy(imageLink = entity.imageLink + "Mod"),
            entity.copy(imageLink = null)
    )

    override fun find(id: Int): Glass? = jdbi.withExtensionUnchecked(GlassDao::class) {
        it.find(id)
    }

    override fun insert(entity: Glass): Int = jdbi.withExtensionUnchecked(GlassDao::class) {
        it.insert(entity)
    }

    override fun update(entity: Glass) = jdbi.useExtensionUnchecked(GlassDao::class) {
        it.update(entity)
    }

    override fun delete(id: Int) = jdbi.useExtensionUnchecked(GlassDao::class) {
        it.delete(id)
    }

    @TestFactory
    fun searchKnown(): Stream<DynamicTest> = sequenceOf("", "h", "H", "l", "L", "hBa", "highball")
            .map { query ->
                dynamicTest("""Search for "$query"""") {
                    val result = jdbi.withExtensionUnchecked(GlassDao::class) {
                        it.search(query)
                    }
                    assertEquals(1, result.size)
                }
            }.asStream()

    @TestFactory
    fun searchUnknown(): Stream<DynamicTest> = sequenceOf("*", "moin", "k")
            .map { query ->
                dynamicTest("""Search for "$query"""") {
                    val result = jdbi.withExtensionUnchecked(GlassDao::class) {
                        it.search(query)
                    }
                    assertEquals(emptyList(), result)
                }
            }.asStream()
}
