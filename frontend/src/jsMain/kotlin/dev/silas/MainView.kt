package dev.silas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.silas.model.Link
import dev.silas.model.NewLink
import dev.silas.model.Update
import dev.silas.view.CustomTextField
import dev.silas.ws.tryWss
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class DetailElement {
    data class LinkElement(val link: Link) : DetailElement()
    object CreateElement : DetailElement()
}

@Composable
fun MainView(
    dependencies: Dependencies
) {
    var links by remember { mutableStateOf(emptyList<Link>()) }
    var selectedElement by remember { mutableStateOf<DetailElement?>(null) }

    LaunchedEffect(true) {
        links = dependencies.linksApi.getAllLink()
        dependencies.httpClient.tryWss("/update") {
            while (true) {
                delay(500L)
                when (val incoming = incoming.receive()) {
                    is Frame.Text -> {
                        val update = dependencies.json.decodeFromString<Update>(incoming.readText())
                        println(update)
                        links = dependencies.linksApi.getAllLink()
                    }
                    else -> println("got $incoming")
                }
            }
        }
    }

    Row(Modifier.fillMaxSize()) {
        Box(
            Modifier.width(250.dp).fillMaxHeight()
                .background(color = Color.LightGray)
        ) {
            LinkList(links, selectedElement, {
                dependencies.notification.notifyReloading()
                dependencies.ioScope.launch {
                    links = dependencies.linksApi.getAllLink()
                }
            }) {
                selectedElement = it
            }
        }

        Spacer(modifier = Modifier.width(1.dp).fillMaxHeight())

        Box(Modifier.fillMaxHeight()) {
            selectedElement?.let {
                when (it) {
                    is DetailElement.LinkElement -> LinkDetailsView(it.link)
                    DetailElement.CreateElement -> CreateLinkDetailsView { fullUrl, shortUrl ->
                        val newLink = when (shortUrl.isNotBlank()) {
                            true -> NewLink(shortUrl, fullUrl)
                            else -> NewLink(null, fullUrl)
                        }
                        dependencies.ioScope.launch {
                            val response = dependencies
                                .linksApi
                                .createLink(newLink)
                            val all = dependencies
                                .linksApi
                                .getAllLink()
                            links = all
                            when (
                                val created = all.firstOrNull { found ->
                                    found.id == response.id
                                }
                            ) {
                                is Link -> {
                                    selectedElement = DetailElement.LinkElement(created)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LinkList(
    links: List<Link>,
    selectedElement: DetailElement?,
    reload: () -> Unit,
    elementSelected: (element: DetailElement) -> Unit
) {
    LazyColumn {
        item {
            ReloadView(reload)
        }
        items(links) { person ->
            LinkView(person, selectedElement, elementSelected)
        }
        item {
            CreateView(elementSelected)
        }
    }
}

@Composable
fun LinkView(
    link: Link,
    selectedElement: DetailElement?,
    elementSelected: (DetailElement) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = { elementSelected(DetailElement.LinkElement(link)) })
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = link.shortUrl,
                style = when (selectedElement) {
                    is DetailElement.LinkElement -> when (link.shortUrl == selectedElement.link.shortUrl) {
                        true -> MaterialTheme.typography.body1
                        else -> MaterialTheme.typography.body1
                    }

                    DetailElement.CreateElement -> MaterialTheme.typography.body1
                    else -> MaterialTheme.typography.body1
                }
            )

            Text(text = link.fullUrl, style = TextStyle(color = Color.DarkGray, fontSize = 14.sp))
        }
    }
}

@Composable
fun CreateView(elementSelected: (DetailElement) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = {
            elementSelected(DetailElement.CreateElement)
        })
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Create"
            )
        }
    }
}

@Composable
fun ReloadView(
    reload: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = reload)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Reload"
            )
        }
    }
}

@Composable
fun LinkDetailsView(link: Link) {
    LazyColumn(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(link) {
            Text(link.shortUrl, style = MaterialTheme.typography.h4)
            Spacer(modifier = Modifier.size(12.dp))
            Text(link.fullUrl, style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.size(12.dp))
            Text(link.createdAt, style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.size(24.dp))
            Text(link.hits.toString(), style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.size(24.dp))
            Text(link.id, style = MaterialTheme.typography.body1)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateLinkDetailsView(create: (String, String) -> Unit) {
    var fullUrl by mutableStateOf(TextFieldValue(""))
    var shortUrl by mutableStateOf(TextFieldValue(""))

    LazyColumn(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                value = fullUrl,
                onValueChange = { input ->
                    fullUrl = input
                },
                label = { Text("full url") },
                isError = { fullUrl.text.isNotEmpty() }
            )
            Spacer(modifier = Modifier.size(12.dp))
            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                value = shortUrl,
                onValueChange = { input ->
                    shortUrl = input
                },
                label = { Text("short url") }
            )
            Spacer(
                modifier = Modifier.size(12.dp)
            )
            Button(onClick = {
                create(fullUrl.text, shortUrl.text)
            }, enabled = fullUrl.text.isNotBlank()) {
                Text(
                    text = "create",
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}
