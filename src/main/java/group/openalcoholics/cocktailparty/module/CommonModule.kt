package group.openalcoholics.cocktailparty.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import io.github.cdimascio.dotenv.dotenv

class CommonModule : AbstractModule() {
    override fun configure() {
    }

    @Provides
    @Singleton
    fun provideDotenv() = dotenv()
}
