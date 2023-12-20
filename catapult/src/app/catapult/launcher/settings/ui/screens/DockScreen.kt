package app.catapult.launcher.settings.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.catapult.launcher.settings
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingSwitch
import com.gmillz.compose.settings.ui.components.SliderSetting
import com.gmillz.compose.settings.util.LocalNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DockScreen() {
    SettingsPage(
        navController = LocalNavController.current,
        title = { Text(text = stringResource(id = R.string.dock_title)) }
    ) {
        val context = LocalContext.current
        SettingGroup {
            SettingSwitch(
                controller = settings.dockEnabled.getController(),
                label = stringResource(id = R.string.show_dock_title)
            )
            SettingSwitch(
                controller = settings.dockSearchBarEnabled.getController(),
                label = stringResource(id = R.string.dock_search_bar_enabled_title)
            )
            SettingSwitch(
                controller = settings.twoRowDockEnabled.getController(),
                label = stringResource(id = R.string.two_row_dock_title)
            )
            SliderSetting(
                label = stringResource(id = R.string.dock_icons_title),
                controller = settings.hotseatColumns.getController(),
                valueRange = 3..10,
                step = 1,
                onChange = {
                    CoroutineScope(Dispatchers.IO).launch {
                        Thread.sleep(1000)
                        LauncherAppState.getInstanceNoCreate().invariantDeviceProfile.onSettingsChanged(context)
                    }
                }
            )
        }
    }
}