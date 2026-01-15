package dev.silas.ws

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.takeFrom


suspend fun HttpClient.tryWss(
    urlString: String,
    request: HttpRequestBuilder.() -> Unit = {},
    block: suspend DefaultClientWebSocketSession.() -> Unit
) {
    runCatching {
        wss(
            {
                url.takeFrom(urlString)
                request()
            },
            block = block
        )
    }.onFailure {
        webSocket(urlString, block = block)
    }


}
