package app.catapult.launcher.settings.ui.screens

import android.app.Activity
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.catapult.launcher.settings
import app.catapult.launcher.settings.ui.components.SettingsLayout
import app.catapult.launcher.smartspace.SmartspaceViewContainer
import app.catapult.launcher.smartspace.model.CatapultSmartspace
import app.catapult.launcher.smartspace.model.SmartspaceCalendar
import app.catapult.launcher.smartspace.model.SmartspaceMode
import app.catapult.launcher.smartspace.model.SmartspaceTimeFormat
import app.catapult.launcher.smartspace.provider.SmartspaceProvider
import com.android.launcher3.R
import com.gmillz.compose.settings.SettingController
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.DividerColumn
import com.gmillz.compose.settings.ui.components.ExpandAndShrink
import com.gmillz.compose.settings.ui.components.ListEntry
import com.gmillz.compose.settings.ui.components.ListSetting
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingSwitch

@Composable
fun SmartspaceScreen(
    fromWidget: Boolean
) {
    val smartspaceController = settings.enableSmartspace.getController()
    val smartspaceModeController = settings.smartspaceMode.getController()
    val smartspaceProvider = SmartspaceProvider.INSTANCE.get(LocalContext.current)
    val modeIsCatapult = smartspaceModeController.state.value == CatapultSmartspace

    SettingsLayout(
        label = stringResource(id = R.string.smartspace_widget)
    ) {
        if (!fromWidget) {
            SettingGroup(description = stringResource(id = R.string.smartspace_widget_toggle_description).takeIf { modeIsCatapult }) {
                SettingSwitch(
                    controller = smartspaceController,
                    label = stringResource(id = R.string.smartspace_widget_toggle_label)
                )
                ExpandAndShrink(visible = smartspaceController.state.value) {
                    SmartspaceProviderSetting(
                        controller = smartspaceModeController,
                    )
                }
            }
        }
        Crossfade(targetState = (smartspaceController.state.value || fromWidget) && modeIsCatapult,
            label = ""
        ) { targetState ->
            if (targetState) {
                Column {
                    SmartspacePreview()
                    SettingGroup(
                        title = stringResource(id = R.string.what_to_show),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        smartspaceProvider.dataSources
                            .asSequence()
                            .filter { it.isAvailable }
                            .forEach {
                                key(it.providerName) {
                                    SettingSwitch(
                                        controller = it.enabledSetting.getController(),
                                        label = stringResource(id = it.providerName),
                                    )
                                }
                            }
                    }
                    SmartspaceDateAndTimeSettings()
                }
            }
        }
    }
}

@Composable
fun SmartspaceProviderSetting(
    controller: SettingController<SmartspaceMode>,
) {

    val context = LocalContext.current

    val entries = remember {
        SmartspaceMode.values().map { mode ->
            ListEntry(
                value = mode,
                label = { stringResource(id = mode.nameResourceId) },
                enabled = mode.isAvailable(context = context)
            )
        }.filter { it.enabled }
    }

    ListSetting(
        controller = controller,
        entries = entries,
        label = stringResource(id = R.string.smartspace_mode_label),
    )
}

@Composable
fun SmartspacePreview() {
    val themeRes = if (isSystemInDarkTheme()) R.style.AppTheme_Dark else R.style.AppTheme_DarkText
    val context = LocalContext.current
    val themedContext = remember(themeRes) { ContextThemeWrapper(context, themeRes) }

    SettingGroup(title = stringResource(id = R.string.preview_label)) {
        CompositionLocalProvider(LocalContext provides themedContext) {
            AndroidView(
                factory = {
                    val view = SmartspaceViewContainer(it, previewMode = true)
                    val height = it.resources
                        .getDimensionPixelSize(R.dimen.enhanced_smartspace_height)
                    view.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, height)
                    view
                },
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 8.dp,
                    bottom = 16.dp,
                ),
            )
        }
        LaunchedEffect(key1 = null) {
            SmartspaceProvider.INSTANCE.get(context).startSetup(context as Activity)
        }
    }
}

@Composable
fun SmartspaceDateAndTimeSettings() {
    SettingGroup(
        title = stringResource(id = R.string.smartspace_date_and_time),
        modifier = Modifier.padding(top = 8.dp),
    ) {

        val calendarSelectionController = settings.smartspaceCalendarSelectionEnabled.getController()
        val calendarController = settings.smartspaceCalendar.getController()
        val showDateController = settings.smartspaceShowDate.getController()
        val showTimeController = settings.smartspaceShowTime.getController()

        val calendarHasMinimumContent = !showDateController.state.value || !showTimeController.state.value

        val calendar = if (calendarSelectionController.state.value) {
            calendarController.state.value
        } else {
            settings.smartspaceCalendar.defaultValue
        }

        ExpandAndShrink(visible = calendar.formatCustomizationSupport) {
            DividerColumn {
                SettingSwitch(
                    controller = showDateController,
                    label = stringResource(id = R.string.smartspace_date),
                    enabled = if (showDateController.state.value) !calendarHasMinimumContent else true,
                )
                ExpandAndShrink(visible = calendarSelectionController.state.value && showDateController.state.value) {
                    SmartspaceCalendarSetting()
                }
                SettingSwitch(
                    controller = showTimeController,
                    label = stringResource(id = R.string.smartspace_time),
                    enabled = if (showTimeController.state.value) !calendarHasMinimumContent else true,
                )
                ExpandAndShrink(visible = showTimeController.state.value) {
                    SmartspaceTimeFormatSetting()
                }
            }
        }
    }
}

@Composable
fun SmartspaceTimeFormatSetting() {

    val entries = remember {
        SmartspaceTimeFormat.values().map { format ->
            ListEntry(
                value = format,
                label = { stringResource(id = format.nameResourceId) },
                enabled = true
            )
        }
    }

    val controller = settings.smartspaceTimeFormat.getController()

    ListSetting(
        controller = controller,
        entries = entries,
        label = stringResource(id = R.string.smartspace_time_format),
    )
}

@Composable
fun SmartspaceCalendarSetting() {

    val entries = remember {
        SmartspaceCalendar.values().map { calendar ->
            ListEntry(
                value = calendar,
                label = { stringResource(id = calendar.nameResourceId) },
                enabled = true
            )
        }
    }

    val controller = settings.smartspaceCalendar.getController()

    ListSetting(
        controller = controller,
        entries = entries,
        label = stringResource(id = R.string.smartspace_calendar),
    )
}