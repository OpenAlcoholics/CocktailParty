package group.openalcoholics.cocktailparty.model

/**
 * Describes the server version.
 * @param apiVersion The API version
 * @param implementation
 */
data class Version(
    val apiVersion: kotlin.String,
    val implementation: ImplementationInfo
)

