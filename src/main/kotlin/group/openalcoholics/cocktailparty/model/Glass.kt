/**
 * API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * OpenAPI spec version: 0.1.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package group.openalcoholics.cocktailparty.model

/**
 * A type of cocktail glass.
 * @param id The glass ID
 * @param name The name of the glass type
 * @param estimatedSize Typical glass size in milliliters
 * @param imageLink A link to an image of the type of glass
 */
data class Glass(
    val id: kotlin.Int,
    val name: kotlin.String,
    val estimatedSize: kotlin.Int,
    val imageLink: kotlin.String? = null
) : BaseModel<Glass> {

    override fun withId(id: Int): Glass {
        return copy(id = id)
    }
}

