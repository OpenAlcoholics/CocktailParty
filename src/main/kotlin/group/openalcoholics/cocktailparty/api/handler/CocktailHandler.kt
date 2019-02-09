package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.api.HandlerController
import group.openalcoholics.cocktailparty.api.NotFoundException
import group.openalcoholics.cocktailparty.api.Status
import group.openalcoholics.cocktailparty.api.bodyAs
import group.openalcoholics.cocktailparty.api.end
import group.openalcoholics.cocktailparty.api.pathId
import group.openalcoholics.cocktailparty.api.setStatus
import group.openalcoholics.cocktailparty.db.dao.CocktailAccessoryCategoryDao
import group.openalcoholics.cocktailparty.db.dao.CocktailDao
import group.openalcoholics.cocktailparty.db.dao.RecipeDao
import group.openalcoholics.cocktailparty.model.Cocktail
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import mu.KotlinLogging
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useHandleUnchecked
import org.jdbi.v3.core.kotlin.withExtensionUnchecked
import java.util.ConcurrentModificationException
import javax.inject.Inject

class CocktailHandler @Inject constructor(private val jdbi: Jdbi) : HandlerController,
    CrudHandler by defaultCrudHandler<Cocktail, CocktailDao>(jdbi) {

    private val logger = KotlinLogging.logger { }

    override fun register(routerFactory: OpenAPI3RouterFactory): Unit = routerFactory.run {
        addHandlerByOperationId("getCocktail", ::get)
        addHandlerByOperationId("insertCocktail", ::insert)
        addHandlerByOperationId("updateCocktail", ::update)
        addHandlerByOperationId("deleteCocktail", ::delete)
        addHandlerByOperationId("searchCocktail", ::search)
    }

    override fun insert(ctx: RoutingContext) {
        val cocktail = ctx.bodyAs<Cocktail>()

        ctx.vertx().executeBlocking({ future: Future<Cocktail> ->
            jdbi.useHandleUnchecked { handle ->
                handle.begin()
                try {
                    val cocktailDao = handle.attach(CocktailDao::class.java)!!
                    val inserted = cocktail.withId(cocktailDao.insert(cocktail))

                    val cocktailIngredientDao = handle.attach(RecipeDao::class.java)!!
                    cocktail.ingredientCategories.forEachIndexed { rank, ingredients ->
                        ingredients.forEach { ingredient ->
                            cocktailIngredientDao.addIngredientCategory(
                                inserted.id,
                                ingredient.ingredientCategoryId,
                                ingredient.share,
                                rank)
                        }
                    }

                    val cocktailAccessoryDao = handle
                        .attach(CocktailAccessoryCategoryDao::class.java)!!
                    cocktail.accessoryCategories.forEach { accessory ->
                        cocktailAccessoryDao.addAccessory(
                            inserted.id,
                            accessory.accessoryCategoryId,
                            accessory.pieces)
                    }

                    handle.commit()
                    future.complete(inserted)
                } catch (failure: Throwable) {
                    handle.rollback()
                    future.fail(failure)
                }
            }
        }, { result ->
            if (result.succeeded()) ctx.response().setStatus(Status.CREATED).end(result.result())
            else ctx.fail(result.cause())
        })
    }

    override fun update(ctx: RoutingContext) {
        val id = ctx.pathId()
        val updated = ctx.bodyAs<Cocktail>().withId(id)

        ctx.vertx().executeBlocking({ future: Future<Cocktail> ->
            jdbi.useHandleUnchecked { handle ->
                val cocktailDao = handle.attach(CocktailDao::class.java)

                @Suppress("UNUSED_VARIABLE")
                val old = cocktailDao.find(id) ?: throw NotFoundException()
                // TODO (use old value to) check authorization

                handle.begin()
                try {
                    cocktailDao.update(updated)

                    handle.attach(RecipeDao::class.java).let { dao ->
                        dao.dropIngredientCategories(id)
                        updated.ingredientCategories.forEachIndexed { rank, ingredients ->
                            ingredients.forEach {
                                dao.addIngredientCategory(
                                    id,
                                    it.ingredientCategoryId,
                                    it.share,
                                    rank)
                            }
                        }
                    }

                    handle.attach(CocktailAccessoryCategoryDao::class.java).let { dao ->
                        dao.dropAccessories(id)
                        updated.accessoryCategories.forEach {
                            dao.addAccessory(id, it.accessoryCategoryId, it.pieces)
                        }
                    }

                    future.complete(cocktailDao.find(id)
                        ?: throw ConcurrentModificationException())
                    handle.commit()
                } catch (failure: Throwable) {
                    handle.rollback()
                    future.fail(failure)
                }
            }
        }, { result ->
            if (result.succeeded()) ctx.response().end(result.result())
            else ctx.fail(result.cause())
        })
    }

    private fun search(ctx: RoutingContext) {
        val query = ctx.queryParam("q").firstOrNull()
        val category = ctx.queryParam("category").firstOrNull()?.toInt()

        ctx.vertx().executeBlocking({ future: Future<List<Cocktail>> ->
            future.complete(jdbi.withExtensionUnchecked(CocktailDao::class) {
                it.search(query, category)
            })
        }, { result ->
            if (result.succeeded()) ctx.response().end(result.result())
            else ctx.fail(result.cause())
        })
    }
}
