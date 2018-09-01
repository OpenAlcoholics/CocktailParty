package group.openalcoholics.cocktailparty.api

import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext

enum class Status(val code: Int) {
    // Success
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204),

    // Client error
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    UNPROCESSABLE_ENTITY(422),

    // Server error
    INTERNAL_SERVER_ERROR(500)
}

fun RoutingContext.fail(status: Status) = fail(status.code)

fun HttpServerResponse.setStatus(status: Status) = this.apply {
    statusCode = status.code
}
