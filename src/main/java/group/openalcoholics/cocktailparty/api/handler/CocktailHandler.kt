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
        if (cocktail.ingredients.asSequence()
                .flatMap {
                    @Suppress("UNCHECKED_CAST")
                    if (it is Ingredient) sequenceOf(it)
                    else (it as List<Ingredient>).asSequence()
                }
                .map { it.share }
                .any { it == null }) {
            return ctx.fail(Status.BAD_REQUEST)
        }
        val inserted = cocktail.withId(jdbi.withExtensionUnchecked(CocktailDao::class) {
            it.insert(cocktail).also { id ->
                cocktail.rankedIngredients { index, ingredient ->
                    it.addIngredient(id, ingredient.id, ingredient.share!!, index)
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

        val (added, removed) = diff(old.ingredients, updated.ingredients)

        val result = jdbi.withExtensionUnchecked(CocktailDao::class) {
            it.update(updated)
            /*     for (ingredient in added) {
                     it.addIngredient(id, ingredient.id, ingredient.share!!, rank = 0)
                 }*/
            /*for (ingredient in removed) {
                it.removeIngredient(id, ingredient.id)
            }
            updated.ingredients.forEachIndexed { index, ingredient ->
                it.updateIngredientRank(id, ingredient.id, index)
            }*/
            it.find(id)
        } ?: return ctx.response().run {
            setStatus(Status.NOT_FOUND)
            end("Cocktail not found. (removed while updating)")
        }

        ctx.response().end(result)
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").firstOrNull()
        val category = ctx.queryParam("category").firstOrNull()?.toInt()
        val alcoholic = ctx.queryParam("alcoholic").firstOrNull()?.toBoolean()
        val cocktails = jdbi.withExtensionUnchecked(CocktailDao::class) {
            it.search(query, category, alcoholic)
        }
        ctx.response().end(cocktails)
    }
}

private data class Diff<E>(val added: Collection<E>, val removed: Collection<E>)

private fun <E> diff(before: Collection<E>, after: Collection<E>): Diff<E> {
    println("Befor: $before\nAfter: $after")
    // TODO This algorithm is bad.
    val beforeSet = before.toSet()
    val afterSet = after.toSet()
    val added = HashSet<E>()
    val removed = HashSet<E>()
    for (e in before) {
        if (e !in afterSet) {
            removed.add(e)
        }
    }
    for (e in after) {
        if (e !in beforeSet) {
            added.add(e)
        }
    }
    println("Added: $added\nRemov: $removed")
    return Diff(added, removed)
}
