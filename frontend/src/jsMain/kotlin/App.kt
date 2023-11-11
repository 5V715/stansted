import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.silas.Dependencies
import dev.silas.Localization
import dev.silas.Notification
import dev.silas.PopupNotification
import dev.silas.view.Toast
import dev.silas.view.ToastState
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.JsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
internal fun App() {
    val toastState = remember { mutableStateOf<ToastState>(ToastState.Hidden) }
    val ioScope: CoroutineScope = rememberCoroutineScope { Dispatchers.Default }
    val dependencies = remember(ioScope) { getDependencies(ioScope, toastState) }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {
            dependencies.notification.notifyExample()
        }) {
            Text("hit ME!")
        }
        Toast(toastState)
    }
}

fun getDependencies(ioScope: CoroutineScope, toastState: MutableState<ToastState>) = object : Dependencies {
    override val httpClient: HttpClient = HttpClient(JsClient())
    override val ioScope: CoroutineScope = ioScope
    override val localization: Localization = object : Localization {
        override val test: String = "Hello"
    }

    override val notification: Notification = object : PopupNotification(localization) {
        override fun showPopUpMessage(text: String) {
            toastState.value = ToastState.Shown(text)
        }
    }
}
