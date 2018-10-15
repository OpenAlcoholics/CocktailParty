package group.openalcoholics.cocktailparty.api

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.inject.Inject
import group.openalcoholics.cocktailparty.api.handler.CocktailCategoryHandler
import group.openalcoholics.cocktailparty.api.handler.CocktailHandler
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
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import io.vertx.kotlin.ext.web.api.contract.RouterFactoryOptions
import mu.KotlinLogging

class Api @Inject constructor(
    private val apiConfig: ApiConfig,
    private val authConfig: AuthConfig,
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

    @Throws(ApiInitializationException::class)
    override fun start(startFuture: Future<Void>?) {
        OpenAPI3RouterFactory.create(vertx, "openapi/OpenCocktail.yaml") { result ->
            if (result.succeeded()) {
                KotlinModule().let {
                    Json.mapper.registerModule(it)
                    Json.prettyMapper.registerModule(it)
                }

                // Spec loaded with success
                val routerFactory = result.result()!!
                routerFactory.options = RouterFactoryOptions(
                    mountNotImplementedHandler = true
                )

                routerFactory
                    .register(versionHandler)
                    .register(glassHandler)
                    .register(ingredientCategoryHandler)
                    .register(ingredientHandler)
                    .register(cocktailCategoryHandler)
                    .register(cocktailHandler)

                routerFactory.addSecurityHandler("Token", SecurityHandler(vertx, authConfig))

                val router = routerFactory.router!!

                val serverOptions = HttpServerOptions().apply {
                    host = apiConfig.host
                    port = apiConfig.port
                }
                val server = vertx.createHttpServer(serverOptions)
                server.requestHandler { router.accept(it) }.listen()
            } else {
                // Something went wrong during router factory initialization
                val exception = result.cause()
                logger.error("Error during RouterFactory initialization", exception)
                throw ApiInitializationException(exception)
            }
        }
    }
}

class ApiInitializationException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
