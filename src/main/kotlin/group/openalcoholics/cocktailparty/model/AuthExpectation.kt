package group.openalcoholics.cocktailparty.model

/**
 * Defines authentication/authorization expectations for an endpoint.
 * @param permissions Required permissions for the endpoint.
 */
data class AuthExpectation(val permissions: List<String>? = null)
