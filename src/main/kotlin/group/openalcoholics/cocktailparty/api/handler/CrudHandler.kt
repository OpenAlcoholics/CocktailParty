package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.api.bodyAs
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.api.fail
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

interface CrudHandler {
    fun get(ctx: RoutingContext)
    fun insert(ctx: RoutingContext)
    fun update(ctx: RoutingContext)
    fun delete(ctx: RoutingContext)
}

inline fun <reified T : BaseModel<T>, reified D : BaseDao<T>> defaultCrudHandler(
    jdbi: Jdbi): CrudHandler =
    defaultCrudHandler(T::class, D::class, jdbi)

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

        ctx.vertx().executeBlocking({ future: Future<T?> ->
            try {
                future.complete(jdbi.withExtensionUnchecked(daoClass) {
                    it.find(id)
                })
            } catch (failure: Throwable) {
                future.fail(failure)
            }
        }, { result ->
            if (result.succeeded()) {
                val entity = result.result()
                if (entity == null) ctx.fail(Status.NOT_FOUND)
                else ctx.response().end(entity)
            } else {
                logger.error(result.cause()) { "Error during get" }
                ctx.fail(Status.INTERNAL_SERVER_ERROR)
            }
        })
    }

    override fun insert(ctx: RoutingContext) {
        val entity = ctx.bodyAs(tClass)
        jdbi.withExtensionUnchecked(daoClass) {
            it.insert(entity)
        }
        ctx.vertx().executeBlocking({ future: Future<T> ->
            try {
                future.complete(entity.withId(jdbi.withExtensionUnchecked(daoClass) {
                    it.insert(entity)
                }))
            } catch (failure: Throwable) {
                future.fail(failure)
            }
        }, { result ->
            if (result.succeeded()) ctx.response().end(result.result())
            else {
                logger.error(result.cause()) { "Error during insert" }
                ctx.fail(Status.INTERNAL_SERVER_ERROR)
            }
        })
    }

    override fun update(ctx: RoutingContext) {
        val updatedEntity = ctx.bodyAs(tClass)
        val id = ctx.pathId()

        ctx.vertx().executeBlocking({ future: Future<T> ->
            jdbi.useHandleUnchecked { handle ->
                val dao = handle.attach(daoClass.java)
                dao.find(id) ?: return@useHandleUnchecked ctx.fail(Status.NOT_FOUND)

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
            else {
                logger.error(result.cause()) { "Error during update" }
                ctx.fail(Status.INTERNAL_SERVER_ERROR)
            }
        })
    }

    override fun delete(ctx: RoutingContext) {
        val id = ctx.pathId()

        ctx.vertx().executeBlocking({ future: Future<Unit?> ->
            try {
                jdbi.useExtensionUnchecked(daoClass) { dao ->
                    dao.delete(id)
                }
                future.complete()
            } catch (failure: Throwable) {
                future.fail(failure)
            }
        }, { result ->
            if (result.succeeded()) ctx.response().setStatus(Status.NO_CONTENT).end()
            else {
                logger.error(result.cause()) { "Error during delete" }
                ctx.fail(Status.INTERNAL_SERVER_ERROR)
            }
        })
    }
}
