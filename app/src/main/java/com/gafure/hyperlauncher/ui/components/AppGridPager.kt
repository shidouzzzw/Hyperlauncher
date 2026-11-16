package com.gafure.hyperlauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gafure.hyperlauncher.model.AppInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private const val GRID_COLUMNS = 4
private const val HOME_CELL_CONTENT_TYPE = "home-app"
private val CELL_HEIGHT = 96.dp
private val ROW_SPACING = 10.dp

/**
 * Swipeable home pages. Each page is a four-column [LazyVerticalGrid] holding one screen of
 * user-selected apps; only the neighbouring page is composed (`beyondViewportPageCount = 1`).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridPager(
    apps: ImmutableList<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppRemove: (AppInfo) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val rows = (((maxHeight - 8.dp) / (CELL_HEIGHT + ROW_SPACING)).toInt()).coerceAtLeast(1)
        val itemsPerPage = rows * GRID_COLUMNS
        val pages = remember(apps, itemsPerPage) {
            apps.chunked(itemsPerPage).map { page -> page.toImmutableList() }
        }
        val pagerState = rememberPagerState(pageCount = { pages.size })

        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            beyondBoundsPageCount = 1,
            verticalAlignment = Alignment.Top,
            key = { pageIndex -> pageIndex },
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            val pageApps = pages.getOrNull(pageIndex) ?: persistentListOf()
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(ROW_SPACING),
                userScrollEnabled = false,
            ) {
                items(
                    items = pageApps,
                    key = { app -> app.key },
                    contentType = { HOME_CELL_CONTENT_TYPE },
                ) { app ->
                    AppGridCell(
                        app = app,
                        onOpen = { onAppClick(app) },
                        onRemoveFromHome = { onAppRemove(app) },
                        onAppInfo = { onAppInfo(app) },
                        modifier = Modifier.height(CELL_HEIGHT),
                    )
                }
            }
        }
    }
}
