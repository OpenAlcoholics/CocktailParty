package group.openalcoholics.cocktailparty.db.dao

import group.openalcoholics.cocktailparty.db.DatabaseTest
import group.openalcoholics.cocktailparty.models.BaseModel
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

interface BaseDaoTest<T : BaseModel<T>> : DatabaseTest, BaseDao<T> {
    fun create(id: Int = 0): T
    fun modifiedVersions(entity: T): Sequence<T>

    @Test
    fun get(jdbi: Jdbi) {
        assertNotNull(find(1))
    }

    @Test
    fun getUnknown(jdbi: Jdbi) {
        assertNull(find(0))
    }

    @Test
    fun insert(jdbi: Jdbi) {
        val entity = create()
        val id = insert(entity)
        assertTrue(id > 0)
        val persisted = find(id)
        assertEquals(entity.withId(id), persisted)
    }

    @TestFactory
    fun update(jdbi: Jdbi): Stream<DynamicTest> {
        val entity = create()
        val id = insert(entity)
        val persistedEntity = entity.withId(id)

        return modifiedVersions(persistedEntity).map { modifiedEntity ->
            dynamicTest("Modified entity: $modifiedEntity") {
                update(modifiedEntity)

                val persistedModified = find(id)

                assertEquals(modifiedEntity, persistedModified)
            }
        }.asStream()
    }

    @Test
    fun delete(jdbi: Jdbi) {
        val entity = create()
        val id = insert(entity)

        delete(id)
    }
}
