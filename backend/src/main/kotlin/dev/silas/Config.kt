package dev.silas

import dev.silas.infra.database.LinkRepository
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.Notification
import io.r2dbc.postgresql.api.PostgresqlResult
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.reactive.asFlow
import kotlinx.serialization.json.Json
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

interface JsonSerializationAccess {
    val json: Json
}

interface PrometheusMeterRegistryAccess {
    val appMicrometerRegistry: PrometheusMeterRegistry
}

interface OtelInstrumentationAccess {
    val openTelemetry: OpenTelemetry
}

data class Config(
    val postgres: DatabaseSettings = DatabaseSettings(),
    val auth: AuthenticationSettings = AuthenticationSettings(),
    val shortLinkLength: Int = 6
) : DatabaseMigration,
    JooqDslAccess,
    LinkRepositoryAccess,
    JsonSerializationAccess,
    PrometheusMeterRegistryAccess,
    OtelInstrumentationAccess {

    override val flyway: Flyway by lazy {
        with(postgres) {
            println(jdbcUrl)
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

    private val connectionFactory by lazy {
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

    private val connectionPool by lazy {
        with(postgres) {
            val configuration = ConnectionPoolConfiguration
                .builder(connectionFactory)
                .maxIdleTime(Duration.ofMillis(1000))
                .maxSize(poolSize)
                .build()
            ConnectionPool(configuration)
        }
    }

    val eventNotification =
        connectionFactory
            .create()
            .flatMapMany { connection ->
                connection.createStatement("LISTEN link_event_notification")
                    .execute()
                    .flatMap(PostgresqlResult::getRowsUpdated)
                    .thenMany(connection.notifications.mapNotNull(Notification::getParameter))
                    .cast(String::class.java)
            }.asFlow()
            .buffer(1)

    override val dslContext by lazy {
        DSL.using(connectionPool)
    }

    override val json: Json by lazy {
        Json {
            prettyPrint = true
            isLenient = true
        }
    }

    override val appMicrometerRegistry: PrometheusMeterRegistry by lazy {
        PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
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

    override val openTelemetry: OpenTelemetry by lazy {
        AutoConfiguredOpenTelemetrySdk.builder().build().openTelemetrySdk
    }

    data class AuthenticationSettings(
        val username: String = "stansted",
        val password: String? = null
    ) {
        fun isEnabled(): Boolean = password != null
    }
}
