package dev.silas

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.silas.model.LinkDetailPage
import dev.silas.model.LinkRepository
import dev.silas.model.LinksOverviewPage
import dev.silas.model.Page
import dev.silas.view.LinksOverviewScreen
import dev.silas.view.NavigationStack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

enum class ExternalEvent {
    Forward, Back
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainView(
    dependencies: Dependencies,
    externalEvents: Flow<ExternalEvent> = emptyFlow()
) {
    val repo = remember { LinkRepository(dependencies) }
    val overviewPage = LinksOverviewPage(repo, externalEvents)
    val navigationStack = remember { NavigationStack<Page>(overviewPage) }

    Surface(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(targetState = navigationStack.lastWithIndex(), transitionSpec = {
            val previousIdx = initialState.index
            val currentIdx = targetState.index
            val multiplier = if (previousIdx < currentIdx) 1 else -1
            if (initialState.value is LinksOverviewPage && targetState.value is LinkDetailPage) {
                fadeIn() with fadeOut(tween(durationMillis = 500, 500))
            } else if (initialState.value is LinkDetailPage && targetState.value is LinksOverviewPage) {
                fadeIn() with fadeOut(tween(delayMillis = 150))
            } else {
                slideInHorizontally { w -> multiplier * w } with
                    slideOutHorizontally { w -> multiplier * -1 * w }
            }
        }) { (index, page) ->
            when (page) {
                is LinksOverviewPage -> {
                    LinksOverviewScreen(
                        page,
                        repo,
                        dependencies,
                        onClickPreviewPicture = { previewPicture ->
                        }
                    )
                }

                is LinkDetailPage -> {
                    /*FullscreenImage(
                        picture = page.pictureData,
                        getFilter = { dependencies.getFilter(it) },
                        localization = dependencies.localization,
                        back = {
                            navigationStack.back()
                        }
                    )*/
                }
            }
        }
    }
}
