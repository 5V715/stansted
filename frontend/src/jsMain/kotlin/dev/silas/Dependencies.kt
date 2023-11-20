package dev.silas

import dev.silas.model.Link
import dev.silas.model.NewLink
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

interface Dependencies {
    val ioScope: CoroutineScope
    val localization: Localization
    val notification: Notification
    val httpClient: HttpClient
    val linksApi: LinksApi
    val json: Json
}

interface LinksApi {
    suspend fun getAllLink(): List<Link>
    suspend fun createLink(newLink: NewLink): Link
}

interface Localization {
    val reloading: String
    val test: String
    val noInternet: String
    val loading: String
}

interface Notification {
    fun notifyExample()
    fun notifyNoInternet()
    fun notifyReloading()
}

abstract class PopupNotification(private val localization: Localization) : Notification {
    abstract fun showPopUpMessage(text: String)

    override fun notifyExample() = showPopUpMessage(localization.test)

    override fun notifyNoInternet() = showPopUpMessage(localization.noInternet)

    override fun notifyReloading() = showPopUpMessage(localization.reloading)
}
