package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.api.bodyAs
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.api.fail
import group.openalcoholics.cocktailparty.api.pathId
import group.openalcoholics.cocktailparty.api.setStatus
import group.openalcoholics.cocktailparty.db.dao.CocktailDao
import group.openalcoholics.cocktailparty.models.Cocktail
import group.openalcoholics.cocktailparty.models.Ingredient
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withExtensionUnchecked

class CocktailHandler(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<Cocktail, CocktailDao>(jdbi) {

    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getCocktail", ::get)
        addHandlerByOperationId("insertCocktail", ::insert)
        addHandlerByOperationId("updateCocktail", ::update)
        addHandlerByOperationId("deleteCocktail", ::delete)
        addHandlerByOperationId("searchCocktail", ::search)
    }

    override fun insert(ctx: RoutingContext) {
        val cocktail = ctx.bodyAs<Cocktail>()
        val inserted = cocktail.withId(jdbi.withExtensionUnchecked(CocktailDao::class) {
            it.insert(cocktail).also { id ->
                cocktail.ingredients.forEachIndexed { rank, ingredients ->
                    ingredients.forEach { ingredient ->
                        it.addIngredient(id, ingredient.ingredientId, ingredient.share, rank)
                    }
                }
            }
        })
        ctx.response().end(inserted)
    }

    override fun update(ctx: RoutingContext) {
        val updated = ctx.bodyAs<Cocktail>()
        val id = ctx.pathId()
        val old = jdbi.withExtensionUnchecked(CocktailDao::class) {
            it.find(id)
        } ?: return ctx.response().run {
            setStatus(Status.NOT_FOUND)
            end("Cocktail not found.")
        }

        val result = jdbi.withExtensionUnchecked(CocktailDao::class) { dao ->
            dao.update(updated)
            dao.dropIngredients(id)
            updated.ingredients.forEachIndexed { rank, ingredients ->
                ingredients.forEach {
                    dao.addIngredient(id, it.ingredientId, it.share, rank)
                }
            }
            dao.find(id)
        } ?: return ctx.response().run {
            setStatus(Status.NOT_FOUND)
            end("Cocktail not found. (removed while updating)")
        }

        ctx.response().end(result)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").firstOrNull()
        val category = ctx.queryParam("category").firstOrNull()?.toInt()
        val cocktails = jdbi.withExtensionUnchecked(CocktailDao::class) {
            it.search(query, category)
        }
        ctx.response().end(cocktails)
    }
}
