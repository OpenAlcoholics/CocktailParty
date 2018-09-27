package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.api.bodyAs
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.api.pathId
import group.openalcoholics.cocktailparty.api.setStatus
import group.openalcoholics.cocktailparty.db.dao.CocktailAccessoryDao
import group.openalcoholics.cocktailparty.db.dao.CocktailDao
import group.openalcoholics.cocktailparty.db.dao.CocktailIngredientDao
import group.openalcoholics.cocktailparty.models.Cocktail
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
            it.insert(cocktail)
        })
        // TODO use a handle for all of this
        jdbi.withExtensionUnchecked(CocktailIngredientDao::class) {
            cocktail.ingredients.forEachIndexed { rank, ingredients ->
                ingredients.forEach { ingredient ->
                    it.addIngredient(inserted.id, ingredient.ingredientId, ingredient.share, rank)
                }
            }
        }
        jdbi.withExtensionUnchecked(CocktailAccessoryDao::class) {
            cocktail.accessories.forEach { accessory ->
                it.addAccessory(inserted.id, accessory.accessoryId, accessory.pieces)
            }
        }
        ctx.response().end(inserted)
    }

    override fun update(ctx: RoutingContext) {
        val updated = ctx.bodyAs<Cocktail>()
        val id = ctx.pathId()

        // TODO check authorization

        val old = jdbi.withExtensionUnchecked(CocktailDao::class) {
            it.find(id)
        } ?: return ctx.response().run {
            setStatus(Status.NOT_FOUND)
            end("Cocktail not found.")
        }

        // TODO use a handle for all of this

        val result = jdbi.withExtensionUnchecked(CocktailDao::class) { dao ->
            dao.update(updated)
            dao.find(id)
        } ?: return ctx.response().run {
            setStatus(Status.NOT_FOUND)
            end("Cocktail not found. (removed while updating)")
        }

        jdbi.withExtensionUnchecked(CocktailIngredientDao::class) { dao ->
            dao.dropIngredients(id)
            updated.ingredients.forEachIndexed { rank, ingredients ->
                ingredients.forEach {
                    dao.addIngredient(id, it.ingredientId, it.share, rank)
                }
            }
        }

        jdbi.withExtensionUnchecked(CocktailAccessoryDao::class) { dao ->
            dao.dropAccessories(id)
            updated.accessories.forEach { dao.addAccessory(id, it.accessoryId, it.pieces) }
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
