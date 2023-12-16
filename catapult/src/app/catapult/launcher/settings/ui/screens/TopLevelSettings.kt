package app.catapult.launcher.settings.ui.screens

import android.app.Activity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.catapult.launcher.settings.Routes
import com.android.launcher3.R
import com.gmillz.compose.settings.ui.SettingsScreen
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.util.LocalNavController

val topLevelScreens = listOf(
    SettingsScreen(
        route = Routes.GENERAL,
        labelRes = R.string.general_title,
        screens = generalScreens
    ) {
        GeneralScreen()
    },
    SettingsScreen(
        route = Routes.HOME_SCREEN,
        labelRes = R.string.homescreen_title,
        screens = homescreenScreens
    ) {
        HomescreenScreen()
    },
    SettingsScreen("dock", R.string.dock_title) {
        DockScreen()
    },
    SettingsScreen(
        route = Routes.DRAWER,
        labelRes = R.string.drawer_title,
        screens = drawerScreens) {
        DrawerScreen()
    },
    SettingsScreen("folders", R.string.folder_title) {
        FolderScreen()
    },
    SettingsScreen(Routes.SMARTSPACE, R.string.smartspace_widget) {
        SmartspaceScreen(fromWidget = false)
    }
)

@Composable
fun TopLevelScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    SettingsPage(
        navController = navController,
        title = { Text(text = stringResource(id = R.string.settings_button_text)) },
        onBack = {
            if (context is Activity) {
                context.finishAndRemoveTask()
            }
        }
    ) {
        SettingGroup {
            topLevelScreens.forEach { screen ->
                SettingTemplate(
                    title = screen.labelRes?.let { stringResource(id = it) }?: screen.route,
                    onClick = {
                        navController.navigate(screen.route)
                    }
                )
            }
        }
    }
}