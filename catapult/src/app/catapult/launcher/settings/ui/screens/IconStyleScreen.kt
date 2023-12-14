package app.catapult.launcher.settings.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.catapult.launcher.data.overrides.ItemOverrideRepository
import app.catapult.launcher.icons.IconPack
import app.catapult.launcher.icons.IconPackProvider
import app.catapult.launcher.settings
import app.catapult.launcher.settings.ui.components.DummyLauncherBox
import app.catapult.launcher.settings.ui.components.DummyLauncherLayout
import app.catapult.launcher.settings.ui.components.WallpaperPreview
import app.catapult.launcher.settings.ui.components.invariantDeviceProfile
import com.android.launcher3.R
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.util.LocalNavController
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun IconStyleScreen() {
    SettingsPage(
        navController = LocalNavController.current,
        title = { Text(text = stringResource(R.string.icon_style)) }
    ) {
        val iconPacks = IconPackProvider.INSTANCE.get(LocalContext.current)
            .getIconPacks().collectAsState(initial = listOf())
        val iconPackController = settings.iconPack.getController()
        val reset = remember { mutableStateOf(0) }
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DummyLauncherBox(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                WallpaperPreview(modifier = Modifier.fillMaxSize())
                key(iconPackController.state.value, reset.value) {
                    DummyLauncherLayout(
                        idp = invariantDeviceProfile(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        SettingGroup(
            title = stringResource(id = R.string.icon_pack)
        ) {

            val lazyListState = rememberLazyListState()
            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    iconPacks.value,
                    key = { item -> item.packPackageName }) { item ->
                    IconPackItem(
                        iconPack = item,
                        selected = item.packPackageName == iconPackController.state.value,
                        modifier = Modifier.width(80.dp)
                    ) {
                        iconPackController.onChange(item.packPackageName)
                    }
                }
            }

        }

        SettingGroup(title = "Icon Pack Settings") {
            SettingTemplate(title = { Text(text = "Remove Custom Icons") }) {
                CoroutineScope(Dispatchers.IO).launch {
                    ItemOverrideRepository.INSTANCE.get(context).deleteAll()
                    reset.value = reset.value + 1
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPackItem(
    iconPack: IconPack,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .size(56.dp)
                    .padding(bottom = 8.dp),
                painter = rememberDrawablePainter(drawable = iconPack.icon),
                contentDescription = null,
                tint = Color.Unspecified
            )
            Text(
                text = iconPack.label,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
