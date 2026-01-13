package dev.silas.ws

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*


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
