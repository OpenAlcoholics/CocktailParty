package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.encodeJson
import group.openalcoholics.cocktailparty.models.ImplementationInfo
import group.openalcoholics.cocktailparty.models.Version
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory

private val version: Version = computeVersion()

private fun computeVersion(): Version {
    // TODO load api version from spec file
    // TODO insert implementation version in build process
    return Version("0.1.0",
        ImplementationInfo("CocktailParty",
            "0.1.0",
            "https://github.com/OpenAlcoholics/CocktailParty"))
}

class VersionHandler : HandlerController {
    private val versionJson = version.encodeJson()

    override fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getVersion", ::getVersion)
    }

    private fun getVersion(ctx: RoutingContext) {
        ctx.response().end(versionJson)
    }
}
