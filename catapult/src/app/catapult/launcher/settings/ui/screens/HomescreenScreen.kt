package app.catapult.launcher.settings.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.launcher3.R
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.util.LocalNavController

@Composable
fun HomescreenScreen() {
    SettingsPage(
        navController = LocalNavController.current,
        title = { Text(text = stringResource(id = R.string.homescreen_title)) }
    ) {

    }
}