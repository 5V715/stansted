package dev.silas

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

interface Dependencies {
    val ioScope: CoroutineScope
    val localization: Localization
    val notification: Notification
    val httpClient: HttpClient
}

interface Localization {
    val test: String
}

interface Notification {
    fun notifyExample()
}

abstract class PopupNotification(private val localization: Localization) : Notification {
    abstract fun showPopUpMessage(text: String)

    override fun notifyExample() = showPopUpMessage(localization.test)
}
