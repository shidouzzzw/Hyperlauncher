package com.gafure.hyperlauncher.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gafure.hyperlauncher.model.AppInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

private val DrawerShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
private val DrawerSurface = Color(0xD90E1014)
private val SearchSurface = Color(0x1AFFFFFF)
private val SearchBorder = Color(0x1AFFFFFF)

private val DrawerLabelShadow = Shadow(
    color = Color.Black.copy(alpha = 0.45f),
    offset = Offset(0f, 1f),
    blurRadius = 6f,
)

private val DrawerLabelStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp,
    color = Color.White,
    shadow = DrawerLabelShadow,
)

private val SearchTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    color = Color.White,
)

private val SearchHintStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    color = Color.White.copy(alpha = 0.45f),
)

private const val DRAWER_CELL_CONTENT_TYPE = "drawer-app"
private val DRAWER_CELL_HEIGHT = 92.dp

/**
 * Full-screen app drawer: swings up with a spring, sits on the blurred home screen, filters as
 * you type and closes on swipe-down, scrim tap or back. Only the drawer surface is drawn above
 * the home content, so the scrolling grid itself is never blurred.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    apps: ImmutableList<AppInfo>,
    onClose: () -> Unit,
    onOpenApp: (AppInfo) -> Unit,
    onAddToHome: (AppInfo) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var closing by remember { mutableStateOf(false) }
    val currentOnClose by rememberUpdatedState(onClose)
    val springSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
    }

    LaunchedEffect(Unit) { progress.animateTo(1f, springSpec) }

    val requestClose: () -> Unit = {
        if (!closing) {
            closing = true
            scope.launch {
                progress.animateTo(0f, springSpec)
                currentOnClose()
            }
        }
    }

    BackHandler { requestClose() }

    var query by remember { mutableStateOf("") }
    val filtered = remember(apps, query) {
        if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.label.contains(query.trim(), ignoreCase = true) }.toImmutableList()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Dim + tap-to-close layer; the home content behind it carries the RenderEffect blur.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value.coerceIn(0f, 1f) }
                .background(Color.Black.copy(alpha = 0.32f))
                .pointerInput(Unit) { detectTapGestures { requestClose() } },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val value = progress.value.coerceIn(0f, 1f)
                    alpha = value
                    translationY = size.height * (1f - value) * 0.35f
                }
                .clip(DrawerShape)
                .background(DrawerSurface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    // Swipe down anywhere on the panel closes the drawer.
                    val threshold = 90.dp.toPx()
                    var dragged = 0f
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragged > threshold) requestClose()
                            dragged = 0f
                        },
                        onDragCancel = { dragged = 0f },
                    ) { change, dragAmount ->
                        dragged += dragAmount
                        change.consume()
                    }
                },
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            DrawerSearchBar(query = query, onQueryChange = { query = it })
            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = filtered,
                    key = { app -> app.key },
                    contentType = { DRAWER_CELL_CONTENT_TYPE },
                ) { app ->
                    DrawerAppCell(
                        app = app,
                        onOpenApp = onOpenApp,
                        onAddToHome = onAddToHome,
                        onAppInfo = onAppInfo,
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = SearchTextStyle,
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SearchSurface)
                    .border(width = 1.dp, color = SearchBorder, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 13.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text(text = "Uygulama ara", style = SearchHintStyle)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun DrawerAppCell(
    app: AppInfo,
    onOpenApp: (AppInfo) -> Unit,
    onAddToHome: (AppInfo) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
) {
    var menuOpen by remember(app.key) { mutableStateOf(false) }
    val currentOnAdd by rememberUpdatedState(onAddToHome)
    val currentOnAppInfo by rememberUpdatedState(onAppInfo)
    val actions = remember(app.key) {
        persistentListOf(
            ContextAction(label = "Ana ekrana ekle") { currentOnAdd(app) },
            ContextAction(label = "Uygulama bilgisi") { currentOnAppInfo(app) },
        )
    }

    Box(
        modifier = Modifier.height(DRAWER_CELL_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppIcon(
                app = app,
                size = 50.dp,
                onClick = { onOpenApp(app) },
                onLongClick = { menuOpen = true },
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.label,
                style = DrawerLabelStyle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppContextMenu(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            actions = actions,
        )
    }
}
