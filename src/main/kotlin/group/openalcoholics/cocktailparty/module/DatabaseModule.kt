package group.openalcoholics.cocktailparty.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import group.openalcoholics.cocktailparty.db.TimestampArgumentFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

class DatabaseModule : AbstractModule() {
    override fun configure() {
        requireBinding(ConfigProvider::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabaseConfig(provider: ConfigProvider) =
        provider.bind<DatabaseConfig>("database")

    @Provides
    @Singleton
    fun provideDataSource(dbConfig: DatabaseConfig): DataSource = HikariConfig().let { config ->
        val jdbcUrl = "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.name}"
        config.jdbcUrl = jdbcUrl
        config.driverClassName = "org.postgresql.Driver"

        config.username = dbConfig.user
        config.password = dbConfig.pass
        config.addDataSourceProperty("dataSourceClassName",
            PGSimpleDataSource::class.java.canonicalName);
        config.addDataSourceProperty("autoCommit", "false");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");

        HikariDataSource(config)
    }

    @Provides
    @Singleton
    fun provideJdbi(dataSource: DataSource): Jdbi = Jdbi.create(dataSource)
        .installPlugin(KotlinPlugin())
        .installPlugin(PostgresPlugin())
        .installPlugin(KotlinSqlObjectPlugin())
        .installPlugin(SqlObjectPlugin())
        .registerArgument(TimestampArgumentFactory())
}

interface DatabaseConfig {
    val host: String
    val port: Int
    val name: String
    val user: String
    val pass: String
}

@Deprecated("Handled by cfg4k now")
class MissingConfigException(key: String) : RuntimeException("Missing config key: $key")
