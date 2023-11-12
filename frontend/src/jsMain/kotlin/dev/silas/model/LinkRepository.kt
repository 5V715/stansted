package dev.silas.model

import dev.silas.Dependencies
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class Link(
    val id: String,
    val shortUrl: String,
    val fullUrl: String,
    val hits: Int,
    val createdAt: String
)

class LinkRepository(val dependencies: Dependencies) {
    private val _repoStateFlow = MutableStateFlow<List<Link>>(listOf())
    val repoStateFlow: StateFlow<List<Link>> = _repoStateFlow

    init {
        loadLinks()
    }

    private fun loadLinks() {
        dependencies.ioScope.launch {
            try {
                val pics = getLinks(dependencies)
                _repoStateFlow.emit(pics)
            } catch (e: CancellationException) {
                println("Rethrowing CancellationException with original cause")
                // https://kotlinlang.org/docs/exception-handling.html#exceptions-aggregation
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                dependencies.notification.notifyNoInternet()
            }
        }
    }

    private suspend fun getLinks(dependencies: Dependencies): List<Link> {
        return dependencies
            .httpClient
            .get("/")
            .call
            .body<List<Link>>()
    }
}
