package dev.silas.infra.server

import dev.silas.JsonSerializationAccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

context(JsonSerializationAccess)
fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(
            json
        )
    }
}
