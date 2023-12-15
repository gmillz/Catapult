package app.catapult.launcher.settings.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.catapult.launcher.settings
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingSwitch
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.ui.components.SliderSetting
import com.gmillz.compose.settings.util.LocalNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DrawerScreen() {
    val context = LocalContext.current
    SettingsPage(
        navController = LocalNavController.current,
        title = { Text(text = stringResource(id = R.string.drawer_title)) }
    ) {
        SettingGroup(
            title = stringResource(id = R.string.general_title)
        ) {
            SliderSetting(
                label = stringResource(id = R.string.background_opacity),
                controller = settings.drawerOpacity.getController(),
                step = 0.1f,
                valueRange = 0f..1f,
                showAsPercentage = true,
            )
        }

        SettingGroup(
            title = stringResource(id = R.string.home_screen_grid)
        ) {
            SliderSetting(
                label = stringResource(id = R.string.app_drawer_columns),
                controller = settings.appDrawerColumns.getController(),
                valueRange = 3..10,
                step = 1,
                onChange = {
                    CoroutineScope(Dispatchers.IO).launch {
                        Thread.sleep(1000)
                        LauncherAppState.getIDP(context).onSettingsChanged(context)
                    }
                }
            )
        }

        SettingGroup(
            title = stringResource(id = R.string.icons_title)
        ) {
            SliderSetting(
                label = stringResource(id = R.string.icon_size_title),
                controller = settings.iconSizeFactorDrawer.getController(),
                valueRange = 0.5f..1.5f,
                step = 0.1f,
                showAsPercentage = true
            )
            SettingSwitch(
                controller = settings.showIconLabelsInDrawer.getController(),
                label = stringResource(id = R.string.show_labels_title)
            )
        }
    }
}