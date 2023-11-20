import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.silas.Dependencies
import dev.silas.LinksApi
import dev.silas.Localization
import dev.silas.MainView
import dev.silas.Notification
import dev.silas.PopupNotification
import dev.silas.api.LinksApiImpl
import dev.silas.style.StanstedTheme
import dev.silas.view.Toast
import dev.silas.view.ToastState
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.JsClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

@Composable
internal fun App() {
    val toastState = remember { mutableStateOf<ToastState>(ToastState.Hidden) }
    val ioScope: CoroutineScope = rememberCoroutineScope { Dispatchers.Default }
    val dependencies = remember(ioScope) { getDependencies(ioScope, toastState) }

    StanstedTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            MainView(dependencies)
            Toast(toastState)
        }
    }
}

fun getDependencies(ioScope: CoroutineScope, toastState: MutableState<ToastState>) = object : Dependencies {
    override val httpClient: HttpClient = HttpClient(JsClient()) {
        install(WebSockets)
        install(ContentNegotiation) {
            json(
                json
            )
        }
    }
    override val linksApi: LinksApi = LinksApiImpl(httpClient)
    override val json: Json = Json {
        ignoreUnknownKeys = true
    }
    override val ioScope: CoroutineScope = ioScope
    override val localization: Localization = object : Localization {
        override val reloading: String = "reloading..."
        override val test: String = "hello"
        override val noInternet: String = "can't load, no internet ?"
        override val loading: String = "Loading.."
    }

    override val notification: Notification = object : PopupNotification(localization) {
        override fun showPopUpMessage(text: String) {
            toastState.value = ToastState.Shown(text)
        }
    }
}
