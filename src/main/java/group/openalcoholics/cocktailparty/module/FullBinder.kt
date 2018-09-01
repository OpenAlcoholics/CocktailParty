package group.openalcoholics.cocktailparty.module

import com.google.inject.AbstractModule

class FullBinder : AbstractModule() {

    override fun configure() {
        install(CommonModule())
        install(DatabaseModule())
        install(WebModule())
    }
}
