package group.openalcoholics.cocktailparty.api.handler

import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.sources.ClasspathConfigSource
import com.jdiazcano.cfg4k.yaml.YamlConfigLoader
import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.encodeJson
import group.openalcoholics.cocktailparty.model.ImplementationInfo
import group.openalcoholics.cocktailparty.model.Version
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import mu.KotlinLogging

private val version: Version = computeVersion()

private fun findProjectVersion(): String {
    val version = PropertyConfigLoader(ClasspathConfigSource("/version.properties"))
        .get("version")?.asString()
    return if (version == null || version == "%APP_VERSION%") {
        KotlinLogging.logger { }.warn {
            "Could not find version in version.properties. This is normal for IDE execution."
        }
        "0.0.0-UNKNOWN"
    } else version
}

private fun findApiVersion(): String {
    val version = YamlConfigLoader(ClasspathConfigSource("/openapi/OpenCocktail.yaml"))
        .get("info.version")?.asString()
    return if (version == null) {
        KotlinLogging.logger { }.error { "Could not find version in OpenCocktail.yaml" }
        "0.0.0-UNKNOWN"
    } else version
}

private fun computeVersion(): Version {
    val projectVersion = findProjectVersion()
    val apiVersion = findApiVersion()
    return Version(apiVersion,
        ImplementationInfo("CocktailParty",
            projectVersion,
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
