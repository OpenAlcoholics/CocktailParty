package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.module.AuthConfig
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.jwt.JWTOptions
import java.util.*

/**
 * Temporary login endpoint with a hardcoded password.
 */
class AdminLoginHandler(
    private val jwtAuth: JWTAuth,
    private val authConfig: AuthConfig) : HandlerController {

    override fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("adminLogin", ::login)
    }

    private fun login(ctx: RoutingContext) {
        val id = UUID.randomUUID()
        val token = jwtAuth.generateToken(json {
            obj()
        }, JWTOptions(
            subject = id.toString(),
            algorithm = "RS${authConfig.sha}",
            permissions = emptyList()
        ))
        ctx.response().end(token)
    }
}
