package group.openalcoholics.cocktailparty.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.inject.Inject
import group.openalcoholics.cocktailparty.api.handler.AdminLoginHandler
import group.openalcoholics.cocktailparty.api.handler.AdminSecurityHandler
import group.openalcoholics.cocktailparty.api.handler.AuthConfigurationException
import group.openalcoholics.cocktailparty.api.handler.CocktailCategoryHandler
import group.openalcoholics.cocktailparty.api.handler.CocktailHandler
import group.openalcoholics.cocktailparty.api.handler.FailureHandler
import group.openalcoholics.cocktailparty.api.handler.GlassHandler
import group.openalcoholics.cocktailparty.api.handler.IngredientCategoryHandler
import group.openalcoholics.cocktailparty.api.handler.IngredientHandler
import group.openalcoholics.cocktailparty.api.handler.SecurityHandler
import group.openalcoholics.cocktailparty.api.handler.VersionHandler
import group.openalcoholics.cocktailparty.module.ApiConfig
import group.openalcoholics.cocktailparty.module.AuthConfig
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import io.vertx.kotlin.ext.web.api.contract.RouterFactoryOptions
import mu.KotlinLogging

class Api @Inject constructor(
    private val apiConfig: ApiConfig,
    private val authConfig: AuthConfig,
    private val jwtAuth: JWTAuth,
    private val versionHandler: VersionHandler,
    private val glassHandler: GlassHandler,
    private val ingredientCategoryHandler: IngredientCategoryHandler,
    private val ingredientHandler: IngredientHandler,
    private val cocktailCategoryHandler: CocktailCategoryHandler,
    private val cocktailHandler: CocktailHandler) : AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    private fun OpenAPI3RouterFactory.register(controller: HandlerController) = this.apply {
        controller.register(this)
    }

    override fun start(startFuture: Future<Void>) {
        OpenAPI3RouterFactory.create(vertx, "openapi/OpenCocktail.yaml") { result ->
            if (result.succeeded()) {
                KotlinModule().let {
                    listOf(Json.mapper, Json.prettyMapper).forEach { mapper ->
                        mapper.registerModule(it)
                        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    }
                }

                // Spec loaded with success
                val routerFactory = result.result()!!
                routerFactory.options = RouterFactoryOptions()

                routerFactory
                    .register(versionHandler)
                    .register(glassHandler)
                    .register(ingredientCategoryHandler)
                    .register(ingredientHandler)
                    .register(cocktailCategoryHandler)
                    .register(cocktailHandler)

                    // TODO this is a temporary endpoint until we have a proper solution
                    .register(AdminLoginHandler(jwtAuth, authConfig))

                try {
                    routerFactory.addSecurityHandler("Token", SecurityHandler(jwtAuth))
                    routerFactory.addSecurityHandler("Admin", AdminSecurityHandler(authConfig))
                } catch (e: AuthConfigurationException) {
                    return@create startFuture.fail(e)
                }

                val router = routerFactory.router!!
                router.exceptionHandler {
                    if (it !is StatusException) {
                        logger.error(it) { "An unknown error occurred." }
                    }
                }
                router.route().failureHandler(FailureHandler())

                val serverOptions = HttpServerOptions().apply {
                    host = apiConfig.host
                    port = apiConfig.port
                }
                val server = vertx.createHttpServer(serverOptions)
                server.requestHandler { router.accept(it) }.listen()
                startFuture.complete()
            } else {
                // Something went wrong during router factory initialization
                val exception = result.cause()
                startFuture.fail(exception)
            }
        }
    }
}
