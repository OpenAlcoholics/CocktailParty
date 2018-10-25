package group.openalcoholics.cocktailparty.api

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import kotlin.reflect.KClass

/**
 * Encode an object as JSON with Jackson.
 */
fun Any.encodeJson(): String = Json.encodePrettily(this)

/**
 * Gets the request body as the specified type.
 * @param T the type to deserialize to
 */
inline fun <reified T> RoutingContext.bodyAs(): T = Json.decodeValue(body, T::class.java)

/**
 * Gets the request body as the specified type.
 * @param clazz the type to deserialize to
 */
fun <T : Any> RoutingContext.bodyAs(clazz: KClass<T>): T = Json.decodeValue(body, clazz.java)

/**
 * Gets the request body as the specified type.
 * @param clazz the type to deserialize to
 */
fun <T : Any> RoutingContext.bodyAs(clazz: Class<T>): T = Json.decodeValue(body, clazz)

/**
 * Extracts the `id` param from the request path as Int.
 */
fun RoutingContext.pathId() = pathParam("id").toInt()

/**
 * Calls [HttpServerResponse.end] with the specified body encoded as JSON.
 * @param body an object that can be serialized to JSON by Jackson
 */
fun HttpServerResponse.end(body: Any) = end(body.encodeJson())

/**
 * Controls the endpoints related to a specific entity.
 */
interface HandlerController {

    /**
     * Registers handlers for the relevant operations by calling
     * [OpenAPI3RouterFactory.addHandlerByOperationId].
     *
     * @param routerFactory the RouterFactory to configure
     */
    fun register(routerFactory: OpenAPI3RouterFactory)
}
