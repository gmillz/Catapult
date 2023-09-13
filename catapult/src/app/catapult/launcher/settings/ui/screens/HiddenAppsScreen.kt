package app.catapult.launcher.settings.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import app.catapult.launcher.settings
import app.catapult.launcher.settings.ui.components.AppItem
import app.catapult.launcher.settings.ui.components.SettingScaffold
import app.catapult.launcher.settings.ui.components.SettingsLazyColumn
import app.catapult.launcher.settings.ui.components.settingGroupItems
import app.catapult.launcher.util.App
import app.catapult.launcher.util.appComparator
import app.catapult.launcher.util.appsState
import com.android.launcher3.R
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.SettingsPage
import java.util.Comparator.comparing

@Composable
fun HiddenAppsScreen() {
    val controller = settings.hiddenApps.getController()
    val hiddenApps by controller.state
    val pageTitle = if (hiddenApps.isEmpty()) {
        stringResource(id = R.string.hidden_apps_title)
    } else {
        stringResource(id = R.string.hidden_apps_title_with_count, hiddenApps.size)
    }
    val apps by appsState(comparator = hiddenAppsComparator(hiddenApps))
    val state = rememberLazyListState()

    SettingScaffold(
        label = pageTitle
    ) { _ ->
        Crossfade(targetState = apps.isNotEmpty(), label = "") { present ->
            if (present) {
                SettingsLazyColumn(state = state) {
                    val toggleHiddenApp = { app: App ->
                        val key = app.key.toString()
                        val newSet = apps.asSequence()
                            .filter { hiddenApps.contains(it.key.toString()) }
                            .map { it.key.toString() }
                            .toMutableSet()
                        val isHidden = !hiddenApps.contains(key)
                        if (isHidden) {
                            newSet.add(key)
                        } else {
                            newSet.remove(key)
                        }
                        controller.onChange(newSet)
                    }

                    settingGroupItems(items = apps) {app ->
                        AppItem(
                            app = app,
                            onClick = toggleHiddenApp,
                            widget = {
                                Checkbox(
                                    checked = hiddenApps.contains(app.key.toString()),
                                    onCheckedChange = null
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun hiddenAppsComparator(hiddenApps: Set<String>): Comparator<App> = remember {
    comparing<App, Int> {
        if (hiddenApps.contains(it.key.toString())) 0 else 1
    }.then(appComparator)
}