package dev.silas.infra.server

import dev.silas.Config
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry

context(Config)
fun Application.configureMonitoring() {
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }
    install(KtorServerTelemetry) {
        setOpenTelemetry(openTelemetry)
    }
}
