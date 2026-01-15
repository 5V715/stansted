package dev.silas

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import dev.silas.api.LinkResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiTest {

    @Test
    fun testGetAll() = runWithTestApplication {
        val response: HttpResponse = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        val entries = response.body<List<LinkResponse>>()
        assertEquals(1, entries.size)
    }

    @Test
    fun testCreate() = runWithTestApplication {
        val response: HttpResponse = client.post("/") {
            contentType(ContentType.Application.Json)
            setBody(SomeCreateRequest)
        }
        assertEquals(HttpStatusCode.Accepted, response.status)
        assertThat(response.body<LinkResponse>()).all {
            prop(LinkResponse::shortUrl).isEqualTo(SomeCreateRequest.shortUrl)
            prop(LinkResponse::fullUrl).isEqualTo(SomeCreateRequest.fullUrl)
        }
    }

    @Test
    fun testGet() = runWithTestApplication {
        val response = client.get("/short")
        assertThat(response.status).isEqualTo(HttpStatusCode.TemporaryRedirect)
        assertThat(response.headers[HttpHeaders.Location]).isEqualTo("https://www.silas.dev")
    }
}
