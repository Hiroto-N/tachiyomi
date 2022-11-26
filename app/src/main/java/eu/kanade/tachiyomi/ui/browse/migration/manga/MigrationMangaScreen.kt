package eu.kanade.tachiyomi.ui.browse.migration.manga

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.MigrateMangaScreen
import eu.kanade.presentation.components.LoadingScreen
import eu.kanade.presentation.util.LocalRouter
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.base.controller.pushController
import eu.kanade.tachiyomi.ui.browse.migration.search.SearchController
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest

data class MigrationMangaScreen(
    private val sourceId: Long,
) : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val router = LocalRouter.currentOrThrow
        val screenModel = rememberScreenModel { MigrationMangaScreenModel(sourceId) }

        val state by screenModel.state.collectAsState()

        if (state.isLoading) {
            LoadingScreen()
            return
        }

        MigrateMangaScreen(
            navigateUp = navigator::pop,
            title = state.source!!.name,
            state = state,
            onClickItem = {
                router.pushController(SearchController(it.id))
            },
            onClickCover = {
                navigator.push(MangaScreen(it.id))
            },
        )

        LaunchedEffect(Unit) {
            screenModel.events.collectLatest { event ->
                when (event) {
                    MigrationMangaEvent.FailedFetchingFavorites -> {
                        context.toast(R.string.internal_error)
                    }
                    MigrationMangaEvent.FailedGettingSource -> {
                        context.toast(R.string.loader_not_implemented_error)
                        router.popCurrentController()
                    }
                }
            }
        }
    }
}