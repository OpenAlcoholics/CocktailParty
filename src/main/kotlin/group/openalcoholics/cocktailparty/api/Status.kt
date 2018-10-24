package group.openalcoholics.cocktailparty.api

import group.openalcoholics.cocktailparty.model.AuthExpectation
import io.vertx.core.http.HttpServerResponse

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

fun HttpServerResponse.setStatus(status: Status) = this.apply {
    statusCode = status.code
}

open class StatusException : Exception {
    val status: Status
    val body: Any?

    constructor(status: Status, body: Any? = null) : super("Failed with status: $status") {
        this.status = status
        this.body = body
    }

    constructor(status: Status, cause: Throwable, body: Any? = null) :
        super("Failed with status $status", cause) {
        this.status = status
        this.body = body
    }
}

class NotFoundException : StatusException(Status.NOT_FOUND)
class AuthException(status: Status, authExpectation: AuthExpectation) :
    StatusException(status, authExpectation)

class InternalServerError : StatusException {
    constructor() : super(Status.INTERNAL_SERVER_ERROR)
    constructor(cause: Throwable) : super(Status.INTERNAL_SERVER_ERROR, cause = cause)
}
