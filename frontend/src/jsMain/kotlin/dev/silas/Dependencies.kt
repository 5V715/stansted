package dev.silas

import dev.silas.model.Link
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

interface Dependencies {
    val ioScope: CoroutineScope
    val localization: Localization
    val notification: Notification
    val httpClient: HttpClient
    val linksApi: LinksApi
}

interface LinksApi {
    suspend fun getAllLink(): List<Link>
    suspend fun createLink(newLink: Link): Link
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
