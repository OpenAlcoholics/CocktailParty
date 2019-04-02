package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.api.bodyAs
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.api.setStatus
import group.openalcoholics.cocktailparty.db.dao.CocktailAccessoryDao
import group.openalcoholics.cocktailparty.model.CocktailAccessory
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useExtensionUnchecked
import org.jdbi.v3.core.kotlin.useHandleUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import java.util.ConcurrentModificationException
import javax.inject.Inject

class CocktailAccessoryHandler @Inject private constructor(
    private val jdbi: Jdbi
) : HandlerController {

    override fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getCocktailAccessories", ::getCocktailAccessories)
        routerFactory.addHandlerByOperationId("addCocktailAccessory", ::addCocktailAccessory)
        routerFactory.addHandlerByOperationId("deleteCocktailAccessory", ::deleteCocktailAccessory)
    }

    private val RoutingContext.cocktailId: Int
        get() = this.pathParam("id").toInt()

    private fun getCocktailAccessories(ctx: RoutingContext) {
        ctx.vertx().executeBlocking({ future: Future<List<CocktailAccessory>> ->
            future.complete(jdbi.withExtensionUnchecked(CocktailAccessoryDao::class) { dao ->
                dao.getAccessories(ctx.cocktailId)
            })
        }, {
            if (it.succeeded()) {
                ctx.response().end(it.result())
            } else {
                ctx.fail(it.cause())
            }
        })
    }

    private fun addCocktailAccessory(ctx: RoutingContext) {
        ctx.vertx().executeBlocking({ future: Future<Unit> ->
            val body = ctx.bodyAs<CocktailAccessory>()
            val cocktailId = ctx.cocktailId

            jdbi.useExtensionUnchecked(CocktailAccessoryDao::class) { dao ->
                dao.addAccessory(
                    cocktailId = ctx.cocktailId,
                    accessoryId = body.accessoryId,
                    pieces = body.pieces
                )
            }
            future.complete()
        }, {
            if (it.succeeded()) {
                ctx.response().setStatus(Status.NO_CONTENT).end()
            } else {
                ctx.fail(it.cause())
            }
        })
    }

    private fun deleteCocktailAccessory(ctx: RoutingContext) {
        ctx.vertx().executeBlocking({ future: Future<Unit> ->
            val accessoryId = ctx.queryParam("accessoryId").first().toInt()
            jdbi.useExtensionUnchecked(CocktailAccessoryDao::class) { dao ->
                dao.deleteAccessory(cocktailId = ctx.cocktailId, accessoryId = accessoryId)
            }
            future.complete()
        }, {
            if (it.succeeded()) {
                ctx.response().setStatus(Status.NO_CONTENT).end()
            } else {
                ctx.fail(it.cause())
            }
        })
    }
}
