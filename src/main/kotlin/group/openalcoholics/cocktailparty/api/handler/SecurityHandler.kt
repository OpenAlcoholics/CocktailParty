package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.AuthException
import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.model.AuthExpectation
import io.vertx.core.Handler
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import mu.KotlinLogging
import javax.inject.Inject

class SecurityHandler @Inject constructor(private val jwtAuth: JWTAuth) : Handler<RoutingContext> {

    private val logger = KotlinLogging.logger {}

    override fun handle(ctx: RoutingContext) {
        // TODO we should find a way to return the required permissions here
        val rawToken: String? = ctx.request().getHeader("Authorization")
        if (rawToken == null
            || !rawToken.startsWith("Bearer ")) {
            logger.debug { "Received token with invalid format: $rawToken" }
            throw AuthException(Status.UNAUTHORIZED, AuthExpectation(null))
        }

        val token = rawToken.substring("Bearer ".length)
        jwtAuth.authenticate(json {
            obj("jwt" to token)
        }) { result ->
            if (result.succeeded()) {
                ctx.setUser(result.result())
                ctx.next()
            } else {
                logger.debug { "Could not verify a token" }
                ctx.fail(AuthException(Status.UNAUTHORIZED, AuthExpectation(null)))
            }
        }
    }
}

class AuthConfigurationException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
