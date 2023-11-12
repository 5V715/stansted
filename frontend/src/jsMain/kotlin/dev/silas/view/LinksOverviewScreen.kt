package dev.silas.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.silas.Dependencies
import dev.silas.ExternalEvent
import dev.silas.model.Link
import dev.silas.model.LinkRepository
import dev.silas.model.LinksOverviewPage

@Composable
internal fun LinksOverviewScreen(
    linksOverviewPage: LinksOverviewPage,
    linkRepository: LinkRepository,
    dependencies: Dependencies,
    onClickPreviewPicture: (Link) -> Unit
) {
    val links = dependencies.links
    var selected: Link? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        linksOverviewPage.externalEvents.collect {
            when (it) {
                ExternalEvent.Forward -> linksOverviewPage.nextImage()
                ExternalEvent.Back -> linksOverviewPage.previousImage()
            }
        }
    }

    Column(modifier = Modifier.background(MaterialTheme.colors.background)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            ListGalleryView(
                links,
                dependencies,
                onSelect = { selected = it },
                onFullScreen = {
                    onClickPreviewPicture(it)
                }
            )
        }
    }
    if (linkRepository.repoStateFlow.value.isEmpty()) {
        LoadingScreen(dependencies.localization.loading)
    }
}

@Composable
private fun ListGalleryView(
    links: SnapshotStateList<Link>,
    dependencies: Dependencies,
    onSelect: (Link) -> Unit,
    onFullScreen: (Link) -> Unit
) {
//    val notification = LocalNotification.current

    ScrollableColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        for ((idx, picWithThumb) in links.withIndex()) {
            Text(picWithThumb.shortUrl)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
