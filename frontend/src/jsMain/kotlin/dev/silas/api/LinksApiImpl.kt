package dev.silas.api

import dev.silas.LinksApi
import dev.silas.model.Link
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class LinksApiImpl(private val httpClient: HttpClient) : LinksApi {
    override suspend fun getAllLink(): List<Link> =
        httpClient
            .get("/")
            .call
            .body<List<Link>>()

    override suspend fun createLink(newLink: Link): Link =
        httpClient
            .post("/") {
                setBody(newLink)
            }.call
            .body()
}
