package group.openalcoholics.cocktailparty

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.providers.CachedConfigProvider
import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.DefaultConfigProvider
import com.jdiazcano.cfg4k.sources.ClasspathConfigSource

class TestConfigModule : AbstractModule() {
    override fun configure() {
    }

    @Provides
    @Singleton
    fun provideConfigProvider(): ConfigProvider {
        val source = ClasspathConfigSource("/config.properties")
        val loader = PropertyConfigLoader(source)
        val provider = DefaultConfigProvider(loader)
        return CachedConfigProvider(provider)
    }
}
