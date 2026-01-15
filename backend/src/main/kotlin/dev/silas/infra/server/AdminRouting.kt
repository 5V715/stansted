package dev.silas.infra.server

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.routing

fun Application.configureAdminRouting() {
    routing {
        staticResources("/admin", "static")
    }
}
