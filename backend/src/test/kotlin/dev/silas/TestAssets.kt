package dev.silas

import dev.silas.Config.AuthenticationSettings
import dev.silas.api.CreateLinkRequest
import dev.silas.domain.Link
import dev.silas.infra.database.LinkRepository
import dev.silas.infra.server.configureApiRouting
import dev.silas.infra.server.configureAuthentication
import dev.silas.infra.server.configureContentNegotiation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.coroutines.EmptyCoroutineContext

val SomeLink = Link(
    id = UUID.randomUUID(),
    shortUrl = "test",
    fullUrl = "https://www.silas.dev",
    hits = emptyList(),
    createdAt = OffsetDateTime.now()
)

val SomeCreateRequest = CreateLinkRequest(shortUrl = "test", fullUrl = "https://www.silas.dev")

val DefaultJsonConfig = Json {
    prettyPrint = true
    isLenient = true
}

val defaultLinkRepositoryBehavior: context(Config) LinkRepository.() -> Unit = {
    coEvery { getAll() } returns listOf(SomeLink)

    coEvery { findAndHit(any(), any()) } returns SomeLink

    val newLink = slot<LinkRepository.NewLink>()
    coEvery { insert(capture(newLink)) } coAnswers {
        Link(
            id = UUID.randomUUID(),
            shortUrl = newLink.captured.shortUrl,
            fullUrl = newLink.captured.fullUrl,
            createdAt = OffsetDateTime.now(),
            hits = emptyList()
        )
    }
}

fun testConfig(
    linkRepositoryBehavior: context(Config) LinkRepository.() -> Unit = defaultLinkRepositoryBehavior
): Config = mockk<Config>().apply {
    val linksRepo = mockk<LinkRepository>()
        .apply {
            linkRepositoryBehavior()
        }

    every { json } returns DefaultJsonConfig

    every { auth } returns AuthenticationSettings()
    every { linkRepository } returns linksRepo
}

fun runWithTestApplication(
    config: Config = testConfig(defaultLinkRepositoryBehavior),
    shouldFollowRedirects: Boolean = false,
    block: suspend ApplicationTestBuilder.() -> Unit
) = with(config) {
    testApplication(EmptyCoroutineContext) {
        client = createClient {
            followRedirects = shouldFollowRedirects
            this.install(ContentNegotiation) {
                json(DefaultJsonConfig)
            }
        }
        application {
            configureAuthentication()
            configureContentNegotiation()
            configureApiRouting()
        }
        block()
    }
}
