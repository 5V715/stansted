package dev.silas.infra.server

import dev.silas.Config
import dev.silas.api.CreateLinkRequest
import dev.silas.api.toResponse
import dev.silas.database.LinkRepository
import dev.silas.domain.Link
import dev.silas.util.RandomAlphaNumeric
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.r2dbc.spi.R2dbcDataIntegrityViolationException

context(Config)
fun Application.routing() {
    routing {
        when (auth.isEnabled()) {
            true -> authenticate(AUTHENTICATION_CONFIG_NAME) {
                adminRoutes()
            }

            else -> adminRoutes()
        }

        publicRoutes()
    }
}

context(Config)
fun Route.publicRoutes() {
    get("/{shortUrl}") {
        when (val param = call.parameters["shortUrl"]) {
            is String -> when (val link = linkRepository.findAndHit(param)) {
                is Link -> with(call.response) {
                    status(HttpStatusCode.TemporaryRedirect)
                    header(io.ktor.http.HttpHeaders.Location, link.fullUrl)
                }

                else -> call.respond(HttpStatusCode.NotFound)
            }

            else -> call.respond(HttpStatusCode.NotFound)
        }
    }
}

context(Config)
fun Route.adminRoutes() {
    staticResources("/admin", "/js/productionExecutable") {
        default("index.html")
    }
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
}