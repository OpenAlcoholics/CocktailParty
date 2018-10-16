package group.openalcoholics.cocktailparty.db

import group.openalcoholics.cocktailparty.TestConfigModule
import group.openalcoholics.cocktailparty.module.DatabaseModule
import name.falgout.jeffrey.testing.junit.guice.GuiceExtension
import name.falgout.jeffrey.testing.junit.guice.IncludeModule
import name.falgout.jeffrey.testing.junit.guice.IncludeModules
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import javax.sql.DataSource

@ExtendWith(GuiceExtension::class)
@IncludeModules(IncludeModule(TestConfigModule::class), IncludeModule(DatabaseModule::class))
interface DatabaseTest {

    @BeforeEach
    fun clean(dataSource: DataSource) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("openapi/sql")
            .load()!!
        flyway.clean()
        flyway.migrate()
    }
}
