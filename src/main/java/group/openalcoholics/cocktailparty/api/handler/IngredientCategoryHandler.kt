package group.openalcoholics.cocktailparty.api.handler

import com.google.inject.Inject
import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.db.dao.IngredientCategoryDao
import group.openalcoholics.cocktailparty.models.IngredientCategory
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked

class IngredientCategoryHandler @Inject constructor(private val jdbi: Jdbi) : HandlerController,
        CrudHandler by defaultCrudHandler<IngredientCategory, IngredientCategoryDao>(jdbi) {
    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getIngredientCategory", ::get)
        addHandlerByOperationId("insertIngredientCategory", ::insert)
        addHandlerByOperationId("updateIngredientCategory", ::update)
        addHandlerByOperationId("deleteIngredientCategory", ::delete)
        addHandlerByOperationId("searchIngredientCategory", ::search)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").first()
        val glasses = jdbi.withExtensionUnchecked(IngredientCategoryDao::class) {
            it.search(query)
        }
        ctx.response().end(glasses)
    }

}
