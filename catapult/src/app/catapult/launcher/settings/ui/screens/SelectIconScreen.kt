package app.catapult.launcher.settings.ui.screens

import android.app.Activity
import android.content.Intent
import android.content.pm.LauncherApps
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import app.catapult.extensions.requireSystemService
import app.catapult.launcher.icons.IconPackProvider
import app.catapult.launcher.icons.IconPickerItem
import app.catapult.launcher.settings.ui.components.AppItem
import app.catapult.launcher.settings.ui.components.SettingsLazyColumn
import app.catapult.launcher.settings.ui.components.settingGroupItems
import com.android.launcher3.util.ComponentKey
import com.gmillz.compose.settings.ui.components.BottomSpacer
import com.gmillz.compose.settings.ui.components.SettingsToolbar
import com.gmillz.compose.settings.util.LocalNavController
import com.gmillz.compose.settings.util.OnResult
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectIconScreen(
    componentKey: ComponentKey
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val launcherApps: LauncherApps = context.requireSystemService()
    val intent = Intent().setComponent(componentKey.componentName)
    val activity = launcherApps.resolveActivity(intent, componentKey.user)
    val label = remember(componentKey) {
        activity.label.toString()
    }
    val originalIcon = rememberDrawablePainter(drawable = activity.getIcon(context.resources.displayMetrics.densityDpi))
    val iconPacks = IconPackProvider.INSTANCE.get(LocalContext.current)
        .getIconPacks().collectAsState(initial = listOf())
    val scope = rememberCoroutineScope()

    OnResult<IconPickerItem> { item ->
        scope.launch {
            (context as Activity).let {
                it.setResult(Activity.RESULT_OK, Intent().apply { putExtra("icon_picker_item", item) })
                it.finish()
            }
        }
    }

    Scaffold(
        topBar = {
            SettingsToolbar(
                title = { Text(text = label) },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                onBack = {
                    if (context is Activity) {
                        context.finishAndRemoveTask()
                    }
                }
            )
        },
        bottomBar = {
            BottomSpacer()
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .consumedWindowInsets(innerPadding)
                .fillMaxHeight()
                .padding(top = innerPadding.calculateTopPadding(), bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = originalIcon,
                    contentDescription = null,
                    modifier = Modifier.requiredSize(50.dp),
                    tint = Color.Unspecified
                )
                Divider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                        .width(1.dp)
                )
                val iconDpi = context.resources.displayMetrics.densityDpi
                val ip = IconPackProvider.INSTANCE.get(context)
                iconPacks.value.forEach {
                    val iconPack = ip.getIconPackOrSystem(it.packPackageName)
                    if (iconPack != null && iconPack.packPackageName != "system") {
                        iconPack.loadBlocking()
                        val iconEntry = iconPack.getIcon(componentKey.componentName)
                        if (iconEntry != null) {
                            val icon = ip.getDrawable(iconEntry, iconDpi, componentKey.user)
                            if (icon != null) {
                                Icon(
                                    painter = rememberDrawablePainter(drawable = icon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .requiredSize(56.dp)
                                        .padding(6.dp)
                                        .clickable {
                                            val iconPickerItem = IconPickerItem(
                                                iconPack.packPackageName,
                                                iconEntry.name,
                                                iconEntry.name,
                                                iconEntry.type
                                            )
                                            scope.launch {
                                                (context as Activity).let { activity ->
                                                    activity.setResult(
                                                        Activity.RESULT_OK, Intent().apply {
                                                            putExtra(
                                                                "icon_picker_item", iconPickerItem
                                                            )
                                                        })
                                                    activity.finish()
                                                }
                                            }
                                        },
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    }
                }
            }

            SettingsLazyColumn {
                settingGroupItems(
                    items = iconPacks.value,
                    title = { "Pick icon for" },
                    key = { it.packPackageName}
                ) {iconPack ->
                    AppItem(
                        label = remember(iconPack) { iconPack.label },
                        icon = remember(iconPack) { iconPack.icon.toBitmap() },
                        onClick = {
                            navController.navigate("pick_icon/${iconPack.packPackageName}")
                        }
                    )
                }
            }
        }
    }
}
