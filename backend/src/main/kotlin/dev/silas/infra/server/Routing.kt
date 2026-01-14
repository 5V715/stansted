package dev.silas.infra.server

import dev.silas.Config
import dev.silas.api.CreateLinkRequest
import dev.silas.api.toResponse
import dev.silas.domain.Link
import dev.silas.infra.database.LinkRepository
import dev.silas.util.RandomAlphaNumeric
import io.ktor.http.HttpHeaders.Location
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.TemporaryRedirect
import io.ktor.server.application.Application
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import kotlinx.coroutines.flow.collectLatest

context(Config)
fun Application.routing() {
    routing {
        webSocket("/update") {
            eventNotification.collectLatest {
                send(Frame.Text(it))
            }
        }
        authenticate(
            AUTHENTICATION_CONFIG_NAME,
            strategy = when (auth.isEnabled()) {
                true -> AuthenticationStrategy.Required
                else -> AuthenticationStrategy.Optional
            }
        ) {
            staticResources("/admin", "static")
            post("/") {
                val request = call.receive<CreateLinkRequest>()
                val shortUrl = when (request.shortUrl) {
                    is String -> request.shortUrl
                    else -> RandomAlphaNumeric(shortLinkLength)
                }
                runCatching {
                    linkRepository.insert(LinkRepository.NewLink(shortUrl = shortUrl, fullUrl = request.fullUrl))
                }.onFailure {
                    when (it) {
                        is R2dbcDataIntegrityViolationException -> call.respond(HttpStatusCode.Conflict)
                        else -> call.respond(HttpStatusCode.BadRequest)
                    }
                    call.respond(HttpStatusCode.BadRequest)
                }.onSuccess {
                    call.respond(HttpStatusCode.Accepted, it!!.toResponse())
                }
            }
            get("/") {
                call.respond(linkRepository.getAll().map { it.toResponse() })
            }
            get("/metrics") {
                call.respond(appMicrometerRegistry.scrape())
            }
        }
        get("/{shortUrl}") {
            val requestHeaders: Map<String, List<String>> =
                call.request.headers.entries().fold(mapOf()) { acc, (key, value) ->
                    acc + (key to value)
                }
            when (val param = call.parameters["shortUrl"]) {
                is String -> when (val link = linkRepository.findAndHit(param, requestHeaders)) {
                    is Link -> with(call.response) {
                        status(TemporaryRedirect)
                        header(Location, link.fullUrl)
                    }

                    else -> call.respond(HttpStatusCode.NotFound)
                }

                else -> call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
