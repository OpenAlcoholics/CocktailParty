package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.*
import group.openalcoholics.cocktailparty.db.dao.BaseDao
import group.openalcoholics.cocktailparty.models.BaseModel
import io.vertx.ext.web.RoutingContext
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import kotlin.reflect.KClass

interface CrudHandler {
    fun get(ctx: RoutingContext)
    fun insert(ctx: RoutingContext)
    fun update(ctx: RoutingContext)
    fun delete(ctx: RoutingContext)
}

inline fun <reified T : BaseModel<T>, reified D : BaseDao<T>> defaultCrudHandler(jdbi: Jdbi): CrudHandler =
        defaultCrudHandler(T::class, D::class, jdbi)

fun <T : BaseModel<T>, D : BaseDao<T>> defaultCrudHandler(tClass: KClass<T>, dClass: KClass<D>, jdbi: Jdbi): CrudHandler =
        DefaultCrudHandler(tClass, dClass, jdbi)

private class DefaultCrudHandler<T : BaseModel<T>, D : BaseDao<T>>(
        private val tClass: KClass<T>,
        private val daoClass: KClass<D>,
        private val jdbi: Jdbi) : CrudHandler {
    override fun get(ctx: RoutingContext) {
        val id = ctx.pathId()
        val entity = jdbi.withExtensionUnchecked(daoClass) {
            it.find(id)
        }

        if (entity == null) ctx.fail(Status.NOT_FOUND)
        else ctx.response().end(entity)
    }

    override fun insert(ctx: RoutingContext) {
        val glass = ctx.bodyAs(tClass)
        val insertedGlass = glass.withId(jdbi.withExtensionUnchecked(daoClass) {
            it.insert(glass)
        })
        ctx.response().end(insertedGlass)
    }

    override fun update(ctx: RoutingContext) {
        val updatedGlass = ctx.bodyAs(tClass)
        val id = ctx.pathId()
        jdbi.withExtensionUnchecked(daoClass) {
            it.find(id)
        } ?: return ctx.fail(Status.NOT_FOUND)

        jdbi.useExtensionUnchecked(daoClass) {
            it.update(updatedGlass)
        }

        val resultGlass = jdbi.withExtensionUnchecked(daoClass) {
            it.find(id)
        } ?: return ctx.fail(Status.NOT_FOUND)

        ctx.response().end(resultGlass)
    }

    override fun delete(ctx: RoutingContext) {
        val id = ctx.pathId()
        jdbi.useExtensionUnchecked(daoClass) {
            it.delete(id)
        }
        ctx.response().setStatus(Status.NO_CONTENT).end()
    }
}
