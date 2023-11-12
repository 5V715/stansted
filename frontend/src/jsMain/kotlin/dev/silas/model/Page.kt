package dev.silas.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.silas.ExternalEvent
import kotlinx.coroutines.flow.Flow

sealed class Page

class LinksOverviewPage(
    val repo: LinkRepository,
    val externalEvents: Flow<ExternalEvent>
) : Page() {

    var currentLinkIndex by mutableStateOf(0)

    val link: Link? = repo.repoStateFlow.value.getOrNull(currentLinkIndex)

    val linkId
        get(): String? = repo.repoStateFlow.value.getOrNull(
            currentLinkIndex
        )?.id

    fun nextImage() {
        currentLinkIndex =
            (currentLinkIndex + 1).mod(repo.repoStateFlow.value.lastIndex)
    }

    fun previousImage() {
        currentLinkIndex =
            (currentLinkIndex - 1).mod(repo.repoStateFlow.value.lastIndex)
    }

    fun selectLink(linkId: String) {
        currentLinkIndex = repo.repoStateFlow.value.indexOfFirst { it.id == linkId }
    }
}

class LinkDetailPage(val link: Link) : Page()
