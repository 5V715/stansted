package dev.silas

import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.silas.model.Link
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

interface Dependencies {
    val ioScope: CoroutineScope
    val localization: Localization
    val notification: Notification
    val httpClient: HttpClient
    val links: SnapshotStateList<Link>
}

interface Localization {
    val test: String
    val noInternet: String
    val loading: String
}

interface Notification {
    fun notifyExample()
    fun notifyNoInternet()
}

abstract class PopupNotification(private val localization: Localization) : Notification {
    abstract fun showPopUpMessage(text: String)

    override fun notifyExample() = showPopUpMessage(localization.test)

    override fun notifyNoInternet() = showPopUpMessage(localization.noInternet)
}
