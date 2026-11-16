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

private const val GRID_COLUMNS = 4
private val CELL_HEIGHT = 96.dp
private val ROW_SPACING = 10.dp

/**
 * Swipeable home pages. Each page is a four-column [LazyVerticalGrid] holding exactly one
 * screen of apps, so vertical scrolling never fights the horizontal [HorizontalPager].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridPager(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val rows = (((maxHeight - 8.dp) / (CELL_HEIGHT + ROW_SPACING)).toInt()).coerceAtLeast(1)
        val itemsPerPage = rows * GRID_COLUMNS
        val pages = remember(apps, itemsPerPage) { apps.chunked(itemsPerPage) }
        val pagerState = rememberPagerState(pageCount = { pages.size })

        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(ROW_SPACING),
                userScrollEnabled = false,
            ) {
                items(items = pages[pageIndex], key = { app -> app.key }) { app ->
                    AppGridCell(
                        app = app,
                        modifier = Modifier.height(CELL_HEIGHT),
                        onClick = { onAppClick(app) },
                    )
                }
            }
        }
    }
}
