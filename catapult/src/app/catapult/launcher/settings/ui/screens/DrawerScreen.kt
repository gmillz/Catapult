package app.catapult.launcher.settings.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.catapult.launcher.settings
import com.android.launcher3.R
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingSwitch
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.ui.components.SliderSetting
import com.gmillz.compose.settings.util.LocalNavController

@Composable
fun DrawerScreen() {
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
            title = stringResource(id = R.string.icons_title)
        ) {
            SettingSwitch(
                controller = settings.showIconLabelsInDrawer.getController(),
                label = stringResource(id = R.string.show_labels_title)
            )
        }
    }
}