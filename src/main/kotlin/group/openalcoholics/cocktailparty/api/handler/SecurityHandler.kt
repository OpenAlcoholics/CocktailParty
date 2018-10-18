package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.api.setStatus
import group.openalcoholics.cocktailparty.model.AuthExpectation
import group.openalcoholics.cocktailparty.module.AuthConfig
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.auth.PubSecKeyOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
import mu.KotlinLogging

class SecurityHandler
@Throws(AuthConfigurationException::class)
constructor(vertx: Vertx, authConfig: AuthConfig) : Handler<RoutingContext> {

    private val logger = KotlinLogging.logger {}
    private val jwtAuth: JWTAuth

    init {
        val sha = authConfig.sha
        if (sha !in listOf(256, 384, 512))
            throw AuthConfigurationException(
                "auth.sha must be one of [256, 384, 512]")
        val jwtOptions = JWTAuthOptions(
            pubSecKeys = listOf(PubSecKeyOptions(
                algorithm = "RS$sha",
                publicKey = authConfig.publicKey)))
        jwtAuth = JWTAuth.create(vertx, jwtOptions)!!
    }

    override fun handle(ctx: RoutingContext) {
        val rawToken: String? = ctx.request().getHeader("Authorization")
        if (rawToken == null
            || !rawToken.startsWith("Bearer ")) {
            logger.debug { "Received token with invalid format: $rawToken" }
            ctx.response()
                .setStatus(Status.UNAUTHORIZED)
                .end(AuthExpectation(null))
        } else {
            val token = rawToken.substring("Bearer ".length)
            jwtAuth.authenticate(json {
                obj("jwt" to token)
            }) { result ->
                if (result.succeeded()) {
                    ctx.setUser(result.result())
                    ctx.next()
                } else {
                    logger.debug { "Could not verify a token" }
                    // TODO we should find a way to return the required permissions here
                    ctx.response()
                        .setStatus(Status.UNAUTHORIZED)
                        .end(AuthExpectation(null))
                }
            }
        }
    }
}

class AuthConfigurationException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
