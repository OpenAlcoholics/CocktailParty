package group.openalcoholics.cocktailparty.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import group.openalcoholics.cocktailparty.api.handler.AuthConfigurationException
import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.ext.auth.PubSecKeyOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
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
    fun provideAuthConfig(provider: ConfigProvider) = provider.bind<AuthConfig>("auth")

    @Provides
    @Singleton
    fun provideJwtAuth(vertx: Vertx, authConfig: AuthConfig): JWTAuth {
        val sha = authConfig.sha
        if (sha !in listOf(256, 384, 512))
            throw AuthConfigurationException(
                "auth.sha must be one of [256, 384, 512]")
        val jwtOptions = JWTAuthOptions(
            pubSecKeys = listOf(PubSecKeyOptions(
                algorithm = "RS$sha",
                publicKey = authConfig.publicKey,
                secretKey = authConfig.privateKey)))
        return JWTAuth.create(vertx, jwtOptions)!!
    }
}

interface ApiConfig {
    val host: String
    val port: Int
}

interface AuthConfig {
    val publicKey: String
    val privateKey: String
    val sha: Int
    val adminKey: String
}
