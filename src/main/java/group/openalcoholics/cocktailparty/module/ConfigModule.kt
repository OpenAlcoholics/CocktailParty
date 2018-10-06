package group.openalcoholics.cocktailparty.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.jdiazcano.cfg4k.loaders.EnvironmentConfigLoader
import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.providers.CachedConfigProvider
import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.DefaultConfigProvider
import com.jdiazcano.cfg4k.providers.OverrideConfigProvider
import com.jdiazcano.cfg4k.sources.ClasspathConfigSource
import com.jdiazcano.cfg4k.sources.FileConfigSource
import com.jdiazcano.cfg4k.yaml.YamlConfigLoader
import mu.KotlinLogging
import java.io.File

class ConfigModule : AbstractModule() {
    private val logger = KotlinLogging.logger { }

    override fun configure() {
    }

    @Provides
    @Singleton
    fun provideConfigProvider(): ConfigProvider {
        val defaultsSource = ClasspathConfigSource("/defaultConfig.properties")
        val defaultsLoader = PropertyConfigLoader(defaultsSource)
        val defaultsProvider = DefaultConfigProvider(defaultsLoader)

        val file = File("config.yaml")
        val fileProvider = if (!file.isFile) {
            logger.warn { "Could not find config file 'config.yaml'" }
            null
        } else {
            val fileSource = FileConfigSource(file)
            val fileLoader = YamlConfigLoader(fileSource)
            DefaultConfigProvider(fileLoader)
        }

        val envLoader = EnvironmentConfigLoader()
        val envProvider = DefaultConfigProvider(envLoader)

        val combinedProvider = if (fileProvider == null) {
            OverrideConfigProvider(envProvider, defaultsProvider)
        } else {
            OverrideConfigProvider(envProvider, fileProvider, defaultsProvider)
        }
        return CachedConfigProvider(combinedProvider)
    }
}
