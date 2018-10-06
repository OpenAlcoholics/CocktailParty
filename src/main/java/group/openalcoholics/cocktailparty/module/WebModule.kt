package group.openalcoholics.cocktailparty.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import group.openalcoholics.cocktailparty.api.handler.CocktailCategoryHandler
import group.openalcoholics.cocktailparty.api.handler.CocktailHandler
import group.openalcoholics.cocktailparty.api.handler.GlassHandler
import group.openalcoholics.cocktailparty.api.handler.IngredientCategoryHandler
import group.openalcoholics.cocktailparty.api.handler.IngredientHandler
import group.openalcoholics.cocktailparty.api.handler.VersionHandler
import org.jdbi.v3.core.Jdbi

class WebModule : AbstractModule() {
    override fun configure() {
        requireBinding(ConfigProvider::class.java)
        requireBinding(Jdbi::class.java)
    }

    @Provides
    @Singleton
    fun provideApiConfig(provider: ConfigProvider) = provider.bind<ApiConfig>("api")

    @Provides
    @Singleton
    fun provideVersionHandler() = VersionHandler()

    @Provides
    @Singleton
    fun provideGlassHandler(jdbi: Jdbi) = GlassHandler(jdbi)

    @Provides
    @Singleton
    fun provideIngredientCategoryHandler(jdbi: Jdbi) = IngredientCategoryHandler(jdbi)

    @Provides
    @Singleton
    fun provideCocktailHandler(jdbi: Jdbi) = CocktailHandler(jdbi)

    @Provides
    @Singleton
    fun provideCocktailCategoryHandler(jdbi: Jdbi) = CocktailCategoryHandler(jdbi)

    @Provides
    @Singleton
    fun provideIngredientHandler(jdbi: Jdbi) = IngredientHandler(jdbi)
}

interface ApiConfig {
    val host: String
    val port: Int
}
