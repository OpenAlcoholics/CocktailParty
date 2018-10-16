package group.openalcoholics.cocktailparty.module

import com.google.inject.AbstractModule

class FullBinder : AbstractModule() {

    override fun configure() {
        install(ConfigModule())
        install(DatabaseModule())
        install(WebModule())
    }
}
