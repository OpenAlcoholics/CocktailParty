package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.db.dao.IngredientDao
import group.openalcoholics.cocktailparty.model.Ingredient
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked

class IngredientHandler(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<Ingredient, IngredientDao>(jdbi) {

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
        val glasses = jdbi.withExtensionUnchecked(IngredientDao::class) {
            it.search(query, category)
        }
        ctx.response().end(glasses)
    }
}
