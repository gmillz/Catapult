package app.catapult.launcher.settings.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.preference.SwitchPreference
import app.catapult.launcher.settings
import com.android.launcher3.R
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.SettingsScreen
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingSwitch
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.ui.components.SliderSetting
import com.gmillz.compose.settings.util.LocalNavController


val drawerScreens = listOf(
    SettingsScreen("hidden_apps") {
        HiddenAppsScreen()
    }
)

@Composable
fun DrawerScreen() {
    val navController = LocalNavController.current
    SettingsPage(
        navController = LocalNavController.current,
        title = { Text(text = stringResource(id = R.string.drawer_title)) }
    ) {
        SettingGroup(
            title = stringResource(id = R.string.general_title)
        ) {
            SliderSetting(
                label = stringResource(id = R.string.background_opacity),
                value = settings.drawerOpacity.getController(),
                step = 0.1f,
                valueRange = 0f..1f,
                showAsPercentage = true,
            )

            SettingTemplate(
                title = stringResource(id = R.string.hidden_apps_title),
                onClick = {
                    navController.navigate("hidden_apps")
                }
            )

            SettingSwitch(
                label = stringResource(id = R.string.show_hidden_apps_search_title),
                controller = settings.showHiddenAppsInSearch.getController()
            )
        }
    }
}