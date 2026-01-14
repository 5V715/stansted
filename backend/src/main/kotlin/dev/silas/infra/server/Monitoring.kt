package dev.silas.infra.server

import dev.silas.Config
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics

context(Config)
fun Application.configureMonitoring() {
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }
}
