package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.db.dao.IngredientDao
import group.openalcoholics.cocktailparty.model.Ingredient
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import mu.KotlinLogging
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import javax.inject.Inject

class IngredientHandler @Inject constructor(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<Ingredient, IngredientDao>(jdbi) {

    private val logger = KotlinLogging.logger { }

    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getIngredient", ::get)
        addHandlerByOperationId("insertIngredient", ::insert)
        addHandlerByOperationId("updateIngredient", ::update)
        addHandlerByOperationId("deleteIngredient", ::delete)
        addHandlerByOperationId("searchIngredient", ::search)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").firstOrNull()
        val category = ctx.queryParam("category").firstOrNull()?.toInt()
        ctx.vertx().executeBlocking({ future: Future<List<Ingredient>> ->
            future.complete(jdbi.withExtensionUnchecked(IngredientDao::class) {
                it.search(query, category)
            })
        }, { result ->
            if (result.succeeded()) ctx.response().end(result.result())
            else ctx.fail(result.cause())
        })
    }
}
