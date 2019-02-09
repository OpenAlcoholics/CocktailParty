package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.db.dao.IngredientCategoryDao
import group.openalcoholics.cocktailparty.model.IngredientCategory
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import mu.KotlinLogging
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import javax.inject.Inject

class IngredientCategoryHandler @Inject constructor(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<IngredientCategory, IngredientCategoryDao>(jdbi) {

    private val logger = KotlinLogging.logger { }

    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getIngredientCategory", ::get)
        addHandlerByOperationId("insertIngredientCategory", ::insert)
        addHandlerByOperationId("updateIngredientCategory", ::update)
        addHandlerByOperationId("deleteIngredientCategory", ::delete)
        addHandlerByOperationId("searchIngredientCategory", ::search)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").firstOrNull()
        ctx.vertx().executeBlocking({ future: Future<List<IngredientCategory>> ->
            future.complete(jdbi.withExtensionUnchecked(IngredientCategoryDao::class) {
                it.search(query)
            })
        }, { result ->
            if (result.succeeded()) ctx.response().end(result.result())
            else ctx.fail(result.cause())
        })
    }

}
