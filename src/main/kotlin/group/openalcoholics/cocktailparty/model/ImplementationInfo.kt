package group.openalcoholics.cocktailparty.model

/**
 * Information about the implementation serving the API.
 * @param name The name of the server implementation.
 * @param version The version of the implementation
 * @param projectInfo URL to the project website
 */
data class ImplementationInfo(
    val name: kotlin.String,
    val version: kotlin.String,
    val projectInfo: kotlin.String? = null
)

