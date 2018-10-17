package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.api.fail
import group.openalcoholics.cocktailparty.db.dao.GlassDao
import group.openalcoholics.cocktailparty.model.Glass
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import mu.KotlinLogging
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked

class GlassHandler(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<Glass, GlassDao>(jdbi) {

    private val logger = KotlinLogging.logger { }

    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getGlass", ::get)
        addHandlerByOperationId("insertGlass", ::insert)
        addHandlerByOperationId("updateGlass", ::update)
        addHandlerByOperationId("deleteGlass", ::delete)
        addHandlerByOperationId("searchGlass", ::search)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").first()
        ctx.vertx().executeBlocking({ future: Future<List<Glass>> ->
            try {
                future.complete(jdbi.withExtensionUnchecked(GlassDao::class) {
                    it.search(query)
                })
            } catch (failure: Throwable) {
                future.fail(failure)
            }
        }, { result ->
            if (result.succeeded()) {
                ctx.response().end(result.result())
            } else {
                logger.error(result.cause()) { "Error during search" }
                ctx.fail(Status.INTERNAL_SERVER_ERROR)
            }
        })
    }
}
