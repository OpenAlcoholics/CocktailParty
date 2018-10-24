package group.openalcoholics.cocktailparty.api

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import kotlin.reflect.KClass

fun Any.encodeJson(): String = Json.encodePrettily(this)

inline fun <reified T> RoutingContext.bodyAs(): T = Json.decodeValue(body, T::class.java)
fun <T : Any> RoutingContext.bodyAs(clazz: KClass<T>): T = Json.decodeValue(body, clazz.java)
fun <T : Any> RoutingContext.bodyAs(clazz: Class<T>): T = Json.decodeValue(body, clazz)

fun RoutingContext.pathId() = pathParam("id").toInt()

fun HttpServerResponse.end(body: Any) = end(body.encodeJson())

interface HandlerController {
    fun register(routerFactory: OpenAPI3RouterFactory)
}
