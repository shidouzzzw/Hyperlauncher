package com.gafure.hyperlauncher.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gafure.hyperlauncher.data.settings.IconSize
import com.gafure.hyperlauncher.data.settings.LauncherSettings
import com.gafure.hyperlauncher.data.settings.ThemeMode
import com.gafure.hyperlauncher.ui.components.HyperCard
import com.gafure.hyperlauncher.ui.components.HyperSectionTitle
import com.gafure.hyperlauncher.ui.components.HyperSegmentedRow
import com.gafure.hyperlauncher.ui.components.HyperSliderRow
import com.gafure.hyperlauncher.ui.components.HyperSwitchRow
import com.gafure.hyperlauncher.ui.theme.HyperRadius
import com.gafure.hyperlauncher.ui.theme.HyperSpacing

/**
 * Settings sheet. Every control writes straight to [SettingsStore] through the callbacks,
 * so what is shown and what the launcher draws can never drift apart.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: LauncherSettings,
    onDismiss: () -> Unit,
    onGridColumnsChange: (Int) -> Unit,
    onDrawerColumnsChange: (Int) -> Unit,
    onIconSizeChange: (IconSize) -> Unit,
    onShowLabelsChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAnimationScaleChange: (Float) -> Unit,
    onScrimStrengthChange: (Float) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val iconSizes = IconSize.entries
    val themeModes = ThemeMode.entries

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = HyperSpacing.lg)
                .padding(bottom = HyperSpacing.xl),
        ) {
            Text(
                text = "Ayarlar",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(HyperSpacing.lg))

            HyperSectionTitle("Ana ekran")
            HyperCard {
                HyperSegmentedRow(
                    title = "Izgara sütunu",
                    options = listOf("4", "5", "6"),
                    selectedIndex = (settings.gridColumns - 4).coerceIn(0, 2),
                    onSelect = { onGridColumnsChange(it + 4) },
                )
                HyperSegmentedRow(
                    title = "İkon boyutu",
                    options = listOf("Küçük", "Orta", "Büyük"),
                    selectedIndex = iconSizes.indexOf(settings.iconSize),
                    onSelect = { onIconSizeChange(iconSizes[it]) },
                )
                HyperSwitchRow(
                    title = "Uygulama adları",
                    subtitle = "Ana ekran ve çekmecede etiketleri göster",
                    checked = settings.showLabels,
                    onCheckedChange = onShowLabelsChange,
                )
            }

            Spacer(modifier = Modifier.height(HyperSpacing.lg))
            HyperSectionTitle("Görünüm")
            HyperCard {
                HyperSegmentedRow(
                    title = "Tema",
                    options = listOf("Sistem", "Açık", "Koyu"),
                    selectedIndex = themeModes.indexOf(settings.themeMode),
                    onSelect = { onThemeModeChange(themeModes[it]) },
                )
                HyperSliderRow(
                    title = "Duvar kağıdı karartması",
                    valueLabel = "${(settings.scrimStrength * 100).toInt()}%",
                    value = settings.scrimStrength,
                    valueRange = 0.2f..1f,
                    onValueChange = onScrimStrengthChange,
                )
            }

            Spacer(modifier = Modifier.height(HyperSpacing.lg))
            HyperSectionTitle("Hareket")
            HyperCard {
                HyperSliderRow(
                    title = "Animasyon hızı",
                    valueLabel = if (settings.animationScale == 0f) {
                        "Kapalı"
                    } else {
                        "%.1fx".format(settings.animationScale)
                    },
                    value = settings.animationScale,
                    valueRange = 0f..2f,
                    steps = 3,
                    onValueChange = onAnimationScaleChange,
                )
            }

            Spacer(modifier = Modifier.height(HyperSpacing.lg))
            HyperSectionTitle("Uygulama çekmecesi")
            HyperCard {
                HyperSegmentedRow(
                    title = "Çekmece sütunu",
                    options = listOf("4", "5", "6"),
                    selectedIndex = (settings.drawerColumns - 4).coerceIn(0, 2),
                    onSelect = { onDrawerColumnsChange(it + 4) },
                )
            }
        }
    }
}
