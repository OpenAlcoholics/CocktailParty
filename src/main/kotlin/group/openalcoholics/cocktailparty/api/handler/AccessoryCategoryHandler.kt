package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.db.dao.AccessoryCategoryDao
import group.openalcoholics.cocktailparty.model.AccessoryCategory
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import javax.inject.Inject

class AccessoryCategoryHandler @Inject constructor(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<AccessoryCategory, AccessoryCategoryDao>(jdbi) {

    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getAccessoryCategory", ::get)
        addHandlerByOperationId("insertAccessoryCategory", ::insert)
        addHandlerByOperationId("updateAccessoryCategory", ::update)
        addHandlerByOperationId("deleteAccessoryCategory", ::delete)
        addHandlerByOperationId("searchAccessoryCategory", ::search)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").firstOrNull()
        ctx.vertx().executeBlocking({ future: Future<List<AccessoryCategory>> ->
            future.complete(jdbi.withExtensionUnchecked(AccessoryCategoryDao::class) {
                it.search(query)
            })
        }, { result ->
            if (result.succeeded()) ctx.response().end(result.result())
            else ctx.fail(result.cause())
        })
    }
}
