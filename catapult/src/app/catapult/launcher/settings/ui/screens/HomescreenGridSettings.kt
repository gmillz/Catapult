package app.catapult.launcher.settings.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.catapult.launcher.settings
import app.catapult.launcher.settings.asSettingController
import app.catapult.launcher.settings.ui.components.GridPreview
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.ui.components.SliderSetting
import com.gmillz.compose.settings.util.LocalNavController

@Composable
fun HomescreenGridSettings() {
    SettingsPage(
        navController = LocalNavController.current,
        title = { Text(text = stringResource(id = R.string.home_screen_grid))}
    ) {
        val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

        val originalColumns = remember { settings.workspaceColumns.firstBlocking() }
        val originalRows = remember { settings.workspaceRows.firstBlocking() }
        val columns = rememberSaveable { mutableIntStateOf(originalColumns) }
        val rows = rememberSaveable { mutableIntStateOf(originalRows) }

        if (isPortrait) {
            GridPreview(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .weight(1f)
                    .align(Alignment.CenterHorizontally)
                    .clip(MaterialTheme.shapes.large)
            ) {
                copy(numColumns = columns.intValue, numRows = rows.intValue)
            }
        }

        SettingGroup {
            SliderSetting(
                label = stringResource(id = R.string.columns),
                controller = columns.asSettingController(),
                valueRange = 3..10,
                step = 1
            )
            SliderSetting(
                label = stringResource(id = R.string.rows),
                controller = rows.asSettingController(),
                valueRange = 3..10,
                step = 1
            )
        }

        val navController = LocalNavController.current
        val context = LocalContext.current
        val applyOverrides = {
            settings.workspaceColumns.set(columns.intValue)
            settings.workspaceRows.set(rows.intValue)
            LauncherAppState.getIDP(context).onSettingsChanged(context)
            navController.popBackStack()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = { applyOverrides() },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(),
                enabled = columns.intValue != originalColumns || rows.intValue != originalRows
            ) {
                Text(text = stringResource(id = R.string.apply_grid))
            }
        }
    }
}