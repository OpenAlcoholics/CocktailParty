package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.StatusException
import group.openalcoholics.cocktailparty.api.end
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

class FailureHandler : Handler<RoutingContext> {

    private val logger = KotlinLogging.logger { }

    override fun handle(ctx: RoutingContext) {
        val failure = ctx.failure()!!
        when (failure) {
            is StatusException ->
                if (failure.body == null)
                    ctx.response().setStatusCode(failure.status.code).end()
                else
                    ctx.response().setStatusCode(failure.status.code).end(failure.body)
            else -> {
                logger.error(failure) { "Unknown error occurred" }
                ctx.next()
            }
        }
    }
}
