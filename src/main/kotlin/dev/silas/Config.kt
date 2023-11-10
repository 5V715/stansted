package dev.silas

import dev.silas.database.LinkRepository
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.flywaydb.core.Flyway

interface DatabaseMigration {
    val flyway: Flyway
}

interface DatabaseConnectionPool {
    val connectionFactory: PostgresqlConnectionFactory
}

interface LinkRepositoryAccess {
    val linkRepository: LinkRepository
}

abstract class DatabaseAccess {
    context(DatabaseConnectionPool)
    suspend fun <T> runQuery(query: String, mapping: (Row, RowMetadata) -> T): List<T>? =
        connectionFactory.create()
            .flatMapMany { connections ->
                connections
                    .createStatement(query)
                    .execute().flatMap { result ->
                        result.map(mapping)
                    }
            }.collectList()
            .awaitSingleOrNull()
}

data class Config(
    val postgres: DatabaseSettings = DatabaseSettings()
) : DatabaseMigration, DatabaseConnectionPool, LinkRepositoryAccess {

    override val flyway: Flyway by lazy {
        with(postgres) {
            Flyway
                .configure()
                .dataSource(
                    jdbcUrl,
                    username,
                    password
                )
                .load()
        }
    }

    override val connectionFactory by lazy {
        with(postgres) {
            PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                    .host(hostname)
                    .port(port)
                    .username(username)
                    .password(password)
                    .database(database) // optional
                    .build()
            )
        }
    }

    data class DatabaseSettings(
        val hostname: String = "localhost",
        val port: Int = 5432,
        val username: String = "postgres",
        val password: String = "postgres",
        val database: String = "postgres"
    ) {
        val jdbcUrl by lazy {
            "jdbc:postgresql://$hostname:$port/$database?user=$username"
        }
    }

    override val linkRepository: LinkRepository by lazy {
        LinkRepository()
    }
}
