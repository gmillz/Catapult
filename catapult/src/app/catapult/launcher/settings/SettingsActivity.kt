package app.catapult.launcher.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.catapult.launcher.settings.ui.screens.TopLevelScreen
import app.catapult.launcher.settings.ui.screens.topLevelScreens
import app.catapult.launcher.settings.ui.theme.SettingsTheme
import com.android.launcher3.R
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
                        }
                    )
                )
            }
        }
    }
}