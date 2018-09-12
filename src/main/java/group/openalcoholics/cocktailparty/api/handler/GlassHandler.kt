package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.db.dao.GlassDao
import group.openalcoholics.cocktailparty.models.Glass
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked

class GlassHandler(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<Glass, GlassDao>(jdbi) {

    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getGlass", ::get)
        addHandlerByOperationId("insertGlass", ::insert)
        addHandlerByOperationId("updateGlass", ::update)
        addHandlerByOperationId("deleteGlass", ::delete)
        addHandlerByOperationId("searchGlass", ::search)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").first()
        val glasses = jdbi.withExtensionUnchecked(GlassDao::class) {
            it.search(query)
        }
        ctx.response().end(glasses)
    }
}
