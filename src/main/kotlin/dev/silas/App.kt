package dev.silas

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import dev.silas.api.CreateLinkRequest
import dev.silas.database.LinkRepository
import dev.silas.domain.Link
import dev.silas.util.RandomAlphaNumeric
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

object App {

    val config = ConfigLoader.builder()
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
        flyway.migrate()
        embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                    }
                )
            }
            routing {
                post("/") {
                    val request = call.receive<CreateLinkRequest>()
                    val shortUrl = when (request.shortUrl) {
                        is String -> request.shortUrl
                        else -> RandomAlphaNumeric(6)
                    }
                    val ids =
                        linkRepository.insert(LinkRepository.NewLink(shortUrl = shortUrl, fullUrl = request.fullUrl))
                    call.respondText("created $ids", status = HttpStatusCode.Created)
                }
                get("/") {
                    call.respondText("got ${LinkRepository().getAll()}")
                }
                get("/{shortUrl}") {
                    when (val param = call.parameters["shortUrl"]) {
                        is String -> when (val link = linkRepository.findByShortUrl(param)) {
                            is Link -> with(call.response) {
                                status(HttpStatusCode.PermanentRedirect)
                                header(HttpHeaders.Location, link.fullFull)
                            }

                            else -> call.respond(HttpStatusCode.NotFound)
                        }

                        else -> call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }.start(wait = true)
    }
}
