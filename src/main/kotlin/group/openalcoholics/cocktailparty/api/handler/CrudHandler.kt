package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.NotFoundException
import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.api.bodyAs
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.api.pathId
import group.openalcoholics.cocktailparty.api.setStatus
import group.openalcoholics.cocktailparty.db.dao.BaseDao
import group.openalcoholics.cocktailparty.model.BaseModel
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.useHandleUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import java.util.*
import kotlin.reflect.KClass

/**
 * CRUD endpoint handler for a specific entity type.
 */
interface CrudHandler {

    /**
     * GET a single entity with an `id` path parameter.
     *
     * Response: 200 with entity body.
     */
    fun get(ctx: RoutingContext)

    /**
     * POST a new entity (from the request body).
     *
     * Response: 201 with entity body.
     */
    fun insert(ctx: RoutingContext)

    /**
     * PUT updated entity (from the request body), with `id` as path parameter.
     *
     * Response: 200 with entity body.
     */
    fun update(ctx: RoutingContext)

    /**
     * DELETE entity based on `id` path parameter.
     *
     * Response: 204, no content.
     */
    fun delete(ctx: RoutingContext)
}

/**
 * Creates a default [CrudHandler] with the specified type parameters.
 *
 * This is a convenience function using reified method type parameters.
 *
 * @param jdbi a JDBI instance
 */
inline fun <reified T : BaseModel<T>, reified D : BaseDao<T>> defaultCrudHandler(
    jdbi: Jdbi): CrudHandler =
    defaultCrudHandler(T::class, D::class, jdbi)

/**
 * Creates a default [CrudHandler] with the specified type parameters.
 *
 * @param tClass the entity type
 * @param dClass the DAO type for the entity
 * @param jdbi a JDBI instance
 * @return a default CrudHandler
 */
fun <T : BaseModel<T>, D : BaseDao<T>> defaultCrudHandler(tClass: KClass<T>, dClass: KClass<D>,
    jdbi: Jdbi): CrudHandler =
    DefaultCrudHandler(tClass, dClass, jdbi)

private class DefaultCrudHandler<T : BaseModel<T>, D : BaseDao<T>>(
    private val tClass: KClass<T>,
    private val daoClass: KClass<D>,
    private val jdbi: Jdbi) : CrudHandler {

    private val logger = KotlinLogging.logger {}

    override fun get(ctx: RoutingContext) {
        val id = ctx.pathId()

        ctx.vertx().executeBlocking({ future: Future<T> ->
            future.complete(jdbi.withExtensionUnchecked(daoClass) {
                it.find(id) ?: throw NotFoundException()
            })
        }, { result ->
            if (result.succeeded()) {
                val entity = result.result()
                ctx.response().end(entity)
            } else ctx.fail(result.cause())
        })
    }

    override fun insert(ctx: RoutingContext) {
        val entity = ctx.bodyAs(tClass)
        ctx.vertx().executeBlocking({ future: Future<T> ->
            future.complete(entity.withId(jdbi.withExtensionUnchecked(daoClass) {
                it.insert(entity)
            }))
        }, { result ->
            if (result.succeeded()) ctx.response().setStatus(Status.CREATED).end(result.result())
            else ctx.fail(result.cause())
        })
    }

    override fun update(ctx: RoutingContext) {
        val id = ctx.pathId()
        val updatedEntity = ctx.bodyAs(tClass).withId(id)

        ctx.vertx().executeBlocking({ future: Future<T> ->
            jdbi.useHandleUnchecked { handle ->
                val dao = handle.attach(daoClass.java)
                dao.find(id) ?: throw NotFoundException()

                handle.begin()
                try {
                    dao.update(updatedEntity)
                    val foundUpdated = dao.find(id) ?: throw ConcurrentModificationException()
                    handle.commit()
                    future.complete(foundUpdated)
                } catch (failure: Throwable) {
                    handle.rollback()
                    future.fail(failure)
                }
            }
        }, { result ->
            if (result.succeeded()) ctx.response().end(result.result())
            else ctx.fail(result.cause())
        })
    }

    override fun delete(ctx: RoutingContext) {
        val id = ctx.pathId()

        ctx.vertx().executeBlocking({ future: Future<Unit?> ->
            jdbi.useExtensionUnchecked(daoClass) { dao ->
                dao.delete(id)
            }
            future.complete()
        }, { result ->
            if (result.succeeded()) ctx.response().setStatus(Status.NO_CONTENT).end()
            else ctx.fail(result.cause())
        })
    }
}
