package dev.silas.infra.server

import dev.silas.Config
import io.ktor.server.application.Application
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic

const val AUTHENTICATION_CONFIG_NAME = "basic-auth"

context(Config)
fun Application.configureAuthentication() {
    authentication {
        basic(name = AUTHENTICATION_CONFIG_NAME) {
            realm = "Admin"
            validate { credentials ->
                if (credentials.name == auth.username &&
                    credentials.password == auth.password
                ) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}
