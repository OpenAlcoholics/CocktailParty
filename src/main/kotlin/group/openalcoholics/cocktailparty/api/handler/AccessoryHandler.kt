package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.db.dao.AccessoryDao
import group.openalcoholics.cocktailparty.model.Accessory
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import javax.inject.Inject

class AccessoryHandler @Inject constructor(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<Accessory, AccessoryDao>(jdbi) {

    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getAccessory", ::get)
        addHandlerByOperationId("insertAccessory", ::insert)
        addHandlerByOperationId("updateAccessory", ::update)
        addHandlerByOperationId("deleteAccessory", ::delete)
        addHandlerByOperationId("searchAccessory", ::search)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").firstOrNull()
        val category = ctx.queryParam("category").firstOrNull()?.toInt()
        val limit = ctx.queryParam("limit").firstOrNull()?.toInt() ?: 40
        val offset = ctx.queryParam("offset").firstOrNull()?.toInt() ?: 0
        ctx.vertx().executeBlocking({ future: Future<List<Accessory>> ->
            future.complete(jdbi.withExtensionUnchecked(AccessoryDao::class) {
                it.search(query, category, limit, offset)
            })
        }, { result ->
            if (result.succeeded()) ctx.response().end(result.result())
            else ctx.fail(result.cause())
        })
    }
}
