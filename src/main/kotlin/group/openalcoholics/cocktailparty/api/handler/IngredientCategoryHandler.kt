package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.db.dao.GenericIngredientDao
import group.openalcoholics.cocktailparty.model.GenericIngredient
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import mu.KotlinLogging
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import javax.inject.Inject

class IngredientCategoryHandler @Inject constructor(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<GenericIngredient, GenericIngredientDao>(jdbi) {

    private val logger = KotlinLogging.logger { }

    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getGenericIngredient", ::get)
        addHandlerByOperationId("insertGenericIngredient", ::insert)
        addHandlerByOperationId("updateGenericIngredient", ::update)
        addHandlerByOperationId("deleteGenericIngredient", ::delete)
        addHandlerByOperationId("searchGenericIngredient", ::search)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").firstOrNull()
        val limit = ctx.queryParam("limit").firstOrNull()?.toInt() ?: 40
        val offset = ctx.queryParam("offset").firstOrNull()?.toInt() ?: 0
        ctx.vertx().executeBlocking({ future: Future<List<GenericIngredient>> ->
            future.complete(jdbi.withExtensionUnchecked(GenericIngredientDao::class) {
                it.search(query, limit, offset)
            })
        }, { result ->
            if (result.succeeded()) ctx.response().end(result.result())
            else ctx.fail(result.cause())
        })
    }

}
