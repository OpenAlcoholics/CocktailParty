package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.api.bodyAs
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.api.fail
import group.openalcoholics.cocktailparty.api.pathId
import group.openalcoholics.cocktailparty.api.setStatus
import group.openalcoholics.cocktailparty.db.dao.BaseDao
import group.openalcoholics.cocktailparty.model.BaseModel
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

    override fun get(ctx: RoutingContext) {
        val id = ctx.pathId()
        val entity = jdbi.withExtensionUnchecked(daoClass) {
            it.find(id)
        }

        if (entity == null) ctx.fail(Status.NOT_FOUND)
        else ctx.response().end(entity)
    }

    override fun insert(ctx: RoutingContext) {
        val entity = ctx.bodyAs(tClass)
        val inserted = entity.withId(jdbi.withExtensionUnchecked(daoClass) {
            it.insert(entity)
        })
        ctx.response().end(inserted)
    }

    override fun update(ctx: RoutingContext) {
        val updatedEntity = ctx.bodyAs(tClass)
        val id = ctx.pathId()
        jdbi.withExtensionUnchecked(daoClass) {
            it.find(id)
        } ?: return ctx.fail(Status.NOT_FOUND)

        jdbi.useExtensionUnchecked(daoClass) {
            it.update(updatedEntity)
        }

        val result = jdbi.withExtensionUnchecked(daoClass) {
            it.find(id)
        } ?: return ctx.fail(Status.NOT_FOUND)

        ctx.response().end(result)
    }

    override fun delete(ctx: RoutingContext) {
        val id = ctx.pathId()
        jdbi.useExtensionUnchecked(daoClass) {
            it.delete(id)
        }
        ctx.response().setStatus(Status.NO_CONTENT).end()
    }
}
