package app.catapult.launcher.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.android.launcher3.LauncherAppState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onEach

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.catapult.launcher.icons.AdaptiveIconDrawableCompat
import app.catapult.launcher.icons.shape.IconShape
import app.catapult.launcher.icons.shape.IconShapeManager
import app.catapult.launcher.launcher
import app.catapult.launcher.smartspace.model.SmartspaceCalendar
import app.catapult.launcher.smartspace.model.SmartspaceMode
import app.catapult.launcher.smartspace.model.SmartspaceTimeFormat
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.graphics.IconShape as L3IconShape
import com.android.launcher3.util.MainThreadInitializedObject
import com.gmillz.compose.settings.BaseSettings
import com.gmillz.compose.settings.SettingController

class Settings(context: Context): BaseSettings(context) {

    private val recreate = { launcher.recreateIfNotScheduled() }
    private val reloadIdp = {
        val idp = InvariantDeviceProfile.INSTANCE.get(context)
        idp.onSettingsChanged(context)
    }

    val dockSearchBarEnabled = setting(
        key = booleanPreferencesKey("dock_search_bar_enabled"),
        defaultValue = true,
        onSet = { recreate() }
    )

    val twoRowDockEnabled = setting(
        key = booleanPreferencesKey("dock_two_row_enabled"),
        defaultValue = false,
        onSet = { recreate() }
    )

    val addIconToHome = setting(
        key = booleanPreferencesKey("add_icon_to_home"),
        defaultValue = false
    )

    val iconPack = setting(
        key = stringPreferencesKey("icon_pack"),
        defaultValue = "",
        onSet = { reloadIdp() }
    )

    val iconShape = setting(
        key = stringPreferencesKey("icon_shape"),
        defaultValue = IconShape.fromString(
            value = "system",
            context = context
        )?: IconShape.Circle,
        parse = { IconShape.fromString(it, context)?: IconShapeManager.getSystemIconShape(context) },
        save = { it.toString() },
        onSet = {
            initializeIconShape(it)
            L3IconShape.init(context)
            LauncherAppState.getInstance(context).reloadIcons()
        }
    )

    val customIconShape = setting(
        key = stringPreferencesKey("custom_icon_shape"),
        defaultValue = IconShape.fromString("system", context)?: IconShape.Circle,
        parse = { IconShape.fromString(value = it, context = context) },
        save = { it.toString() },
        onSet = { it?.let(iconShape::setBlocking) }
    )

    val enableFeed = setting(
        key = booleanPreferencesKey("enable_feed"),
        defaultValue = true,
        onSet = { recreate() }
    )

    val feedProvider = setting(
        key = stringPreferencesKey("feed_provider"),
        defaultValue = "com.google.android.googlequicksearchbox",
        onSet = { recreate() }
    )

    val showNotificationCount = setting(
        key = booleanPreferencesKey("show_notification_count"),
        defaultValue = false,
        onSet = { reloadIdp() }
    )

    // Smartspace
    val enableSmartspace = setting(
        key = booleanPreferencesKey("enable_smartspace"),
        defaultValue = true,
        onSet = { recreate() }
    )
    val smartspaceMode = setting(
        key = stringPreferencesKey("smartspace_mode"),
        defaultValue = SmartspaceMode.fromString("lawnchair"),
        parse = { SmartspaceMode.fromString(it) },
        save = { it.toString() },
        onSet = { recreate() }
    )
    val smartspaceCalendarSelectionEnabled = setting(
        key = booleanPreferencesKey("smartspace_calendar_selection_enabled"),
        defaultValue = false
    )
    val smartspaceBatteryStatus = setting(
        key = booleanPreferencesKey("enable_smartspace_battery_status"),
        defaultValue = false
    )
    val smartspaceNowPlaying = setting(
        key = booleanPreferencesKey("enable_smartspace_now_playing"),
        defaultValue = true
    )
    val smartspaceCalendar = setting(
        key = stringPreferencesKey("smartspace_calendar"),
        defaultValue = SmartspaceCalendar.fromString("gregorian"),
        parse = { SmartspaceCalendar.fromString(it) },
        save = { it.toString() }
    )
    val smartspaceAagWidget = setting(
        key = booleanPreferencesKey("enable_smartspace_aag_widget"),
        defaultValue = true
    )
    val smartspaceShowDate = setting(
        key = booleanPreferencesKey("smartspace_show_date"),
        defaultValue = true
    )
    val smartspaceShowTime = setting(
        key = booleanPreferencesKey("smartspace_show_time"),
        defaultValue = false
    )
    val smartspaceTimeFormat = setting(
        key = stringPreferencesKey("smartspace_time_format"),
        defaultValue = SmartspaceTimeFormat.fromString("system"),
        parse = { stringValue -> SmartspaceTimeFormat.fromString(stringValue)  },
        save = { timeFormat -> timeFormat.toString() }
    )

    val allowWidgetOverlap = setting(
        key = booleanPreferencesKey("allow_widget_overlap"),
        defaultValue = false,
        onSet = { recreate() }
    )

    val showTopShadow = setting(
        key = booleanPreferencesKey("show_top_shadow"),
        defaultValue = false
    )

    val drawerOpacity = setting(
        key = floatPreferencesKey("drawer_background_opacity"),
        defaultValue = 1f,
        onSet = { recreate() }
    )

    val hotseatColumns = setting(
        key = intPreferencesKey("hotseat_columns"),
        defaultValue = -1,
    )

    val workspaceRows = setting(
        key = intPreferencesKey("workspace_rows"),
        defaultValue = -1,
    )

    val workspaceColumns = setting(
        key = intPreferencesKey("workspace_columns"),
        defaultValue = -1,
    )

    init {
        initializeIconShape(iconShape.firstBlocking())
        iconShape.get()
            .drop(1)
            .distinctUntilChanged()
            .onEach { shape ->
                initializeIconShape(shape)
                L3IconShape.init(context)
                LauncherAppState.getInstance(context).reloadIcons()
            }
    }

    private fun initializeIconShape(shape: IconShape) {
        AdaptiveIconDrawableCompat.sInitialized = true
        AdaptiveIconDrawableCompat.sMask = shape.getMaskPath()
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::Settings)

        @JvmStatic
        fun getInstance(context: Context): Settings = INSTANCE.get(context)
    }
}

private class MutableStateSettingController<T>(
    private val mutableState: MutableState<T>
): SettingController<T> {
    override val state = mutableState

    override fun onChange(newValue: T) {
        mutableState.value = newValue
    }
}

@Composable
fun <T> MutableState<T>.asSettingController(): SettingController<T> {
    return remember(this) { MutableStateSettingController(this) }
}