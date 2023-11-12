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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.silas.model.Link
import kotlinx.coroutines.launch

@Composable
fun MainView(
    dependencies: Dependencies
) {
    var links by remember { mutableStateOf(emptyList<Link>()) }
    var selectedLink by remember { mutableStateOf<Link?>(null) }

    LaunchedEffect(true) {
        links = dependencies.linksApi.getAllLink()
    }

    Row(Modifier.fillMaxSize()) {
        Box(
            Modifier.width(250.dp).fillMaxHeight()
                .background(color = Color.LightGray)
        ) {
            LinkList(links, selectedLink, {
                dependencies.notification.notifyReloading()
                dependencies.ioScope.launch {
                    links = dependencies.linksApi.getAllLink()
                }
            }) {
                selectedLink = it
            }
        }

        Spacer(modifier = Modifier.width(1.dp).fillMaxHeight())

        Box(Modifier.fillMaxHeight()) {
            selectedLink?.let {
                LinkDetailsView(it)
            }
        }
    }
}

@Composable
fun LinkList(
    links: List<Link>,
    selectedLink: Link?,
    reload: () -> Unit,
    linkSelected: (link: Link) -> Unit
) {
    LazyColumn {
        item {
            ReloadView(reload)
        }
        items(links) { person ->
            LinkView(person, selectedLink, linkSelected)
        }
    }
}

@Composable
fun LinkView(
    link: Link,
    selectedLink: Link?,
    linkSelected: (link: Link) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = { linkSelected(link) })
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = link.shortUrl,
                style = if (link.shortUrl == selectedLink?.shortUrl) MaterialTheme.typography.h6 else MaterialTheme.typography.body1
            )

            Text(text = link.fullUrl, style = TextStyle(color = Color.DarkGray, fontSize = 14.sp))
        }
    }
}

@Composable
fun ReloadView(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
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
