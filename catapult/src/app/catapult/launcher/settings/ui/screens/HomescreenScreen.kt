package app.catapult.launcher.settings.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.catapult.launcher.feed.FeedBridge
import app.catapult.launcher.settings
import com.android.launcher3.R
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.ListEntry
import com.gmillz.compose.settings.ui.components.ListSetting
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingSwitch
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.util.LocalNavController
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun HomescreenScreen() {
    SettingsPage(
        navController = LocalNavController.current,
        title = { Text(text = stringResource(id = R.string.homescreen_title)) }
    ) {
        SettingGroup {
            val context = LocalContext.current
            val entries = arrayListOf<ListEntry<String>>()
            FeedBridge.INSTANCE.get(context).bridgePackages.forEach {
                if (it.isAvailable()) {
                    entries.add(ListEntry(
                        value = it.packageName,
                        label = { context.packageManager.let { pm -> pm.getApplicationInfo(it.packageName, 0).loadLabel(pm).toString() }},
                        icon = {
                            Icon(
                                modifier = Modifier.size(36.dp),
                                painter = rememberDrawablePainter(drawable = context.packageManager.let { pm -> pm.getApplicationInfo(it.packageName, 0).loadIcon(pm) }),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                    ))
                }
            }
            ListSetting(
                controller = settings.feedProvider.getController(),
                entries = entries,
                label = stringResource(id = R.string.feed_provider_title)
            )
        }
        
        SettingGroup(
            title = stringResource(id = R.string.wallpaper_button_text)
        ) {
            SettingSwitch(
                controller = settings.showTopShadow.getController(),
                label = stringResource(id = R.string.show_top_shadow_title)
            )
        }
        
        SettingGroup(
            title = stringResource(id = R.string.widget_button_text)
        ) {
            SettingSwitch(
                controller = settings.allowWidgetOverlap.getController(),
                label = stringResource(id = R.string.allow_widget_overlap)
            )
        }

    }
}