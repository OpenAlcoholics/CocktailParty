package group.openalcoholics.cocktailparty.db

import group.openalcoholics.cocktailparty.module.CommonModule
import group.openalcoholics.cocktailparty.module.DatabaseModule
import io.github.cdimascio.dotenv.Dotenv
import name.falgout.jeffrey.testing.junit.guice.GuiceExtension
import name.falgout.jeffrey.testing.junit.guice.IncludeModule
import name.falgout.jeffrey.testing.junit.guice.IncludeModules
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import javax.sql.DataSource

@ExtendWith(GuiceExtension::class)
@IncludeModules(IncludeModule(CommonModule::class), IncludeModule(DatabaseModule::class))
interface DatabaseTest {
    @BeforeEach
    fun clean(dotenv: Dotenv, dataSource: DataSource) {
        dotenv.get("")

        val flyway = Flyway()
        flyway.dataSource = dataSource
        flyway.setLocations("migration/sql")
        flyway.clean()
        flyway.migrate()
    }
}
