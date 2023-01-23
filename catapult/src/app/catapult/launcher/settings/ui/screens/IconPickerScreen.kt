package app.catapult.launcher.settings.ui.screens

import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.catapult.extensions.requireSystemService
import app.catapult.launcher.icons.CustomIconPack
import app.catapult.launcher.icons.IconPack
import app.catapult.launcher.icons.IconPackProvider
import app.catapult.launcher.icons.IconPickerItem
import app.catapult.launcher.settings.ui.components.LazyGridLayout
import app.catapult.launcher.settings.ui.components.SettingsLazyColumn
import app.catapult.launcher.settings.ui.components.verticalGridItems
import com.gmillz.compose.settings.util.resultSender
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Composable
fun IconPickerScreen(
    packageName: String
) {
    val context = LocalContext.current
    val iconPack = remember {
        IconPackProvider.INSTANCE.get(context).getIconPackOrSystem(packageName)
    } ?: return

    var searchQuery by remember { mutableStateOf("") }
    val onClickItem = resultSender<IconPickerItem>()

    val pickerComponent = remember {
        val launcherApps: LauncherApps = context.requireSystemService()
        launcherApps.getActivityList(iconPack.packPackageName, Process.myUserHandle())
            .firstOrNull()?.componentName
    }
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            val icon =
                it.data?.getParcelableExtra<Intent.ShortcutIconResource>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
                    ?: return@rememberLauncherForActivityResult
            val entry = (iconPack as CustomIconPack).createFromExternalPicker(icon)
                ?: return@rememberLauncherForActivityResult
            onClickItem(entry)
        }
    )

    Column(Modifier.fillMaxWidth()) {
        IconPickerGrid(iconPack = iconPack, searchQuery = searchQuery, onClickItem = onClickItem)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconPickerGrid(
    iconPack: IconPack,
    searchQuery: String,
    onClickItem: (item: IconPickerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var loadFailed by remember { mutableStateOf(false) }
    val categoriesFlow = remember {
        iconPack.getAllIcons()
            .catch { loadFailed = true }
    }
    val categories by categoriesFlow.collectAsState(initial = emptyList())
    val filteredCategories by remember {
        derivedStateOf {
            categories.asSequence()
                .map { it.filter(searchQuery) }
                .filter { it.items.isNotEmpty() }
                .toList()
        }
    }

    val density = LocalDensity.current

    val gridLayout = remember {
        LazyGridLayout(
            minWidth = 56.dp,
            gapWidth = 16.dp,
            density = density
        )
    }

    val numColumns by gridLayout.numColumns

    SettingsLazyColumn(
        modifier = modifier.then(gridLayout.onSizeChanged())
    ) {
        if (numColumns != 0) {
            filteredCategories.forEach { category ->
                stickyHeader {
                    Text(
                        text = category.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                verticalGridItems(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    items = category.items,
                    numColumns = numColumns,
                ) { _, item ->
                    IconPreview(
                        iconPack = iconPack,
                        iconItem = item,
                        onClick = {
                            onClickItem(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun IconPreview(
    iconPack: IconPack,
    iconItem: IconPickerItem,
    onClick: () -> Unit
) {
    val drawable by produceState<Drawable?>(initialValue = null, iconPack, iconItem) {
        launch(Dispatchers.IO) {
            value = iconPack.getIcon(iconItem.toIconEntry(), 0)
        }
    }
    Box(
        modifier = Modifier
            .clip(Shapes().small)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            painter = rememberDrawablePainter(drawable = drawable),
            contentDescription = iconItem.drawableName,
            modifier = Modifier.aspectRatio(1f),
            tint = Color.Unspecified
        )
    }
}
