package app.catapult.launcher.settings.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.catapult.launcher.settings
import app.catapult.launcher.settings.Routes
import app.catapult.launcher.settings.ui.components.NotificationDotsSetting
import app.catapult.launcher.settings.ui.components.notificationDotsEnabled
import app.catapult.launcher.settings.ui.components.notificationServiceEnabled
import com.android.launcher3.R
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.SettingsScreen
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingSwitch
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.util.LocalNavController

val generalScreens = listOf(
    SettingsScreen("icon_style") {
        IconStyleScreen()
    },
    SettingsScreen(Routes.ICON_SHAPE) {
        IconShapeScreen()
    },
    SettingsScreen(Routes.CUSTOM_ICON_SHAPE_CREATOR) {
        CustomIconShapeCreatorScreen()
    }
)

@Composable
fun GeneralScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    SettingsPage(
        navController = navController,
        title = { Text(text = stringResource(id = R.string.general_title)) }
    ) {
        SettingGroup(
            title = stringResource(id = R.string.icon_category)
        ) {
            SettingTemplate(
                title = stringResource(id = R.string.icon_style),
                onClick = {
                    navController.navigate("icon_style")
                }
            )
            SettingTemplate(
                title = stringResource(id = R.string.icon_shape_title),
                onClick = {
                    navController.navigate(Routes.ICON_SHAPE)
                }
            )
        }
        SettingGroup(
            title = stringResource(id = R.string.notification_dots_title)
        ) {
            val enabled by remember {
                notificationDotsEnabled(context)
            }.collectAsState(initial = false)

            val serviceEnabled = notificationServiceEnabled()

            NotificationDotsSetting(
                enabled = enabled,
                serviceEnabled = serviceEnabled)
            if (enabled && serviceEnabled) {
                SettingSwitch(
                    controller = settings.showNotificationCount.getController(),
                    label = stringResource(id = R.string.show_notification_count)
                )
            }
        }
    }
}