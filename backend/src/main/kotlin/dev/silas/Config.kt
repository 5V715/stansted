package dev.silas

import dev.silas.database.LinkRepository
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.time.Duration

interface DatabaseMigration {
    val flyway: Flyway
}

interface JooqDslAccess {
    val dslContext: DSLContext
}

interface LinkRepositoryAccess {
    val linkRepository: LinkRepository
}

data class Config(
    val postgres: DatabaseSettings = DatabaseSettings(),
    val auth: AuthenticationSettings = AuthenticationSettings(),
    val shortLinkLength: Int = 6
) : DatabaseMigration, JooqDslAccess, LinkRepositoryAccess {

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

    private val connectionPool by lazy {
        with(postgres) {
            val connectionFactory = PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                    .host(hostname)
                    .port(port)
                    .username(username)
                    .password(password)
                    .database(database) // optional
                    .build()
            )

            val configuration = ConnectionPoolConfiguration.builder(connectionFactory)
                .maxIdleTime(Duration.ofMillis(1000))
                .maxSize(poolSize)
                .build()
            ConnectionPool(configuration)
        }
    }

    override val dslContext by lazy {
        DSL.using(connectionPool)
    }

    data class DatabaseSettings(
        val hostname: String = "localhost",
        val port: Int = 5432,
        val username: String = "postgres",
        val password: String = "postgres",
        val database: String = "postgres",
        val poolSize: Int = 5
    ) {
        val jdbcUrl by lazy {
            "jdbc:postgresql://$hostname:$port/$database?user=$username"
        }
    }

    override val linkRepository: LinkRepository by lazy {
        LinkRepository()
    }

    data class AuthenticationSettings(
        val username: String = "stansted",
        val password: String? = null
    ) {
        fun isEnabled(): Boolean = password != null
    }
}
