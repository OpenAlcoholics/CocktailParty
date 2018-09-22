package group.openalcoholics.cocktailparty.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import group.openalcoholics.cocktailparty.db.TimestampArgumentFactory
import io.github.cdimascio.dotenv.Dotenv
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

class DatabaseModule : AbstractModule() {
    override fun configure() {
        requireBinding(Dotenv::class.java)
    }

    private fun Dotenv.getSafe(key: String) = get(key) ?: throw MissingConfigException(key)

    @Provides
    @Singleton
    fun provideDatabaseConfig(dotenv: Dotenv): DatabaseConfig {
        val host = dotenv.getSafe(HOST)
        val port = dotenv.getSafe(PORT)
        val name = dotenv.getSafe(NAME)
        val user = dotenv.getSafe(USER)
        val pass = dotenv.getSafe(PASS)
        return DatabaseConfig(host, port, name, user, pass)
    }

    @Provides
    @Singleton
    fun provideDataSource(dbConfig: DatabaseConfig): DataSource = HikariConfig().let { config ->
        val jdbcUrl = "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.name}"
        config.dataSourceClassName = PGSimpleDataSource::class.java.name

        config.addDataSourceProperty("serverName", dbConfig.host)
        config.addDataSourceProperty("portNumber", dbConfig.port)
        config.addDataSourceProperty("databaseName", dbConfig.name)
        config.addDataSourceProperty("user", dbConfig.user)
        config.isAutoCommit = false

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

    private companion object {
        val HOST = "DB_HOST"
        val PORT = "DB_PORT"
        val NAME = "DB_NAME"
        val USER = "DB_USER"
        val PASS = "DB_PASS"
    }
}

data class DatabaseConfig(
    val host: String,
    val port: String,
    val name: String,
    val user: String,
    val pass: String)

class MissingConfigException(key: String) : RuntimeException("Missing config key: $key")
