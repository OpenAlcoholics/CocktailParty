package group.openalcoholics.cocktailparty.db

import org.jdbi.v3.core.argument.AbstractArgumentFactory
import org.jdbi.v3.core.argument.Argument
import org.jdbi.v3.core.config.ConfigRegistry
import java.sql.Types
import java.time.OffsetDateTime

/**
 * Inserts OffsetDateTime as epoch seconds.
 */
class TimestampArgumentFactory : AbstractArgumentFactory<OffsetDateTime>(Types.BIGINT) {

    override fun build(value: OffsetDateTime, config: ConfigRegistry): Argument {
        return Argument { position, statement, _ ->
            statement.setLong(position, value.toEpochSecond())
        }
    }
}
