package dev.silas

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import dev.silas.infra.server.configureAdminRouting
import dev.silas.infra.server.configureApiRouting
import dev.silas.infra.server.configureAuthentication
import dev.silas.infra.server.configureContentNegotiation
import dev.silas.infra.server.configureMonitoring
import dev.silas.infra.server.configureWebSockets
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging

object App {

    private val logger = KotlinLogging.logger {}

    private val config = ConfigLoader.builder()
        .addPropertySources(
            listOf(
                EnvironmentVariablesPropertySource(
                    useUnderscoresAsSeparator = true,
                    allowUppercaseNames = true
                ),
                SystemPropertiesPropertySource()
            )
        )
        .build()
        .loadConfigOrThrow<Config>()

    @JvmStatic
    fun main(args: Array<String>): Unit = with(config) {
        logger.info { "starting database migration" }
        flyway.migrate()
        logger.info { "authentication enabled: ${auth.isEnabled()}" }
        embeddedServer(Netty, port = 8080) {
            configureAuthentication()
            configureContentNegotiation()
            configureWebSockets()
            configureMonitoring()
            configureApiRouting()
            configureAdminRouting()
        }.start(wait = true)
    }
}
