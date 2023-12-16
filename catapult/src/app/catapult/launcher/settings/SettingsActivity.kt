package app.catapult.launcher.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.catapult.extensions.identifier
import app.catapult.launcher.settings.ui.screens.IconPickerScreen
import app.catapult.launcher.settings.ui.screens.SelectIconScreen
import app.catapult.launcher.settings.ui.screens.SmartspaceScreen
import app.catapult.launcher.settings.ui.screens.TopLevelScreen
import app.catapult.launcher.settings.ui.screens.topLevelScreens
import app.catapult.launcher.settings.ui.theme.SettingsTheme
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.gmillz.compose.settings.ui.SettingsScreen
import com.gmillz.compose.settings.ui.SettingsSurface

class SettingsActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SettingsTheme {
                SettingsSurface(
                    startDestination = "top_level",
                    screens = listOf(
                        SettingsScreen("top_level", R.string.settings_button_text, topLevelScreens) {
                            TopLevelScreen()
                        },
                        SettingsScreen(
                            route = "select_icon/{packageName}/{component}/{user}",
                            arguments = listOf(
                                navArgument("packageName") { type = NavType.StringType },
                                navArgument("component") { type = NavType.StringType },
                                navArgument("user") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val args = backStackEntry.arguments!!
                            val packageName = args.getString("packageName")
                            val component = args.getString("component")
                            val user = args.getString("user")
                            val key = ComponentKey.fromString("$packageName/$component#$user")!!
                            SelectIconScreen(key)
                        },
                        SettingsScreen(
                            route = "pick_icon/{packageName}",
                            arguments = listOf(
                                navArgument("packageName") { type = NavType.StringType }
                            )
                        ) {
                            val packageName = it.arguments!!.getString("packageName")!!
                            IconPickerScreen(packageName)
                        },
                        SettingsScreen(Routes.SMARTSPACE_WIDGET, R.string.smartspace_widget) {
                            SmartspaceScreen(fromWidget = true)
                        }
                    )
                )
            }
        }
    }

    companion object {
        fun createSelectIconIntent(context: Context, componentKey: ComponentKey): Intent {
            val uri =
                "android-app://androidx.navigation/select_icon/${componentKey.componentName.flattenToString()}/${componentKey.user.identifier}".toUri()
            return Intent(Intent.ACTION_VIEW, uri, context, SettingsActivity::class.java)
        }

        fun getIntent(context: Context, route: String): Intent {
            val uri ="android-app://androidx.navigation/$route".toUri()
            return Intent(Intent.ACTION_VIEW, uri, context, SettingsActivity::class.java)
        }

        fun start(context: Context, route: String) {
            context.startActivity(getIntent(context, route))
        }
    }
}

object Routes {
    const val SMARTSPACE = "smartspace"
    const val SMARTSPACE_WIDGET = "smartspace_widget"
    const val GENERAL = "general"
    const val CUSTOM_ICON_SHAPE_CREATOR = "custom_icon_shape_creator"
    const val ICON_SHAPE = "icon_shape"
    const val HOME_SCREEN = "home_screen"
    const val HOME_SCREEN_GRID = "home_screen_grid"
    const val DRAWER = "drawer"
    const val DRAWER_FOLDERS = "drawer_folders"
}