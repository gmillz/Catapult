package app.catapult.launcher.settings.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.launcher3.R
import com.gmillz.compose.settings.ui.SettingsScreen
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.util.LocalNavController

val generalScreens = listOf(
    SettingsScreen("icon_style") {
        IconStyleScreen()
    }
)

@Composable
fun GeneralScreen() {
    val navController = LocalNavController.current
    SettingsPage(
        navController = navController,
        title = { Text(text = stringResource(id = R.string.general_title)) }
    ) {
        SettingGroup {
            SettingTemplate(
                title = stringResource(id = R.string.icon_style),
                onClick = {
                    navController.navigate("icon_style")
                }
            )
        }
    }
}