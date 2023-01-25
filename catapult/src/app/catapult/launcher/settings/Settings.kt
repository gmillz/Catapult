package app.catapult.launcher.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.catapult.launcher.launcher
import app.catapult.launcher.smartspace.model.SmartspaceCalendar
import app.catapult.launcher.smartspace.model.SmartspaceMode
import app.catapult.launcher.smartspace.model.SmartspaceTimeFormat
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.util.MainThreadInitializedObject
import com.gmillz.compose.settings.BaseSettings

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

    val iconPack = setting(
        key = stringPreferencesKey("icon_pack"),
        defaultValue = "",
        onSet = { reloadIdp() }
    )

    val enableFeed = setting(
        key = booleanPreferencesKey("enable_feed"),
        defaultValue = true,
        onSet = { recreate() }
    )

    val feedProvider = setting(
        key = stringPreferencesKey("feed_provider"),
        defaultValue = ""
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

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::Settings)

        @JvmStatic
        fun getInstance(context: Context): Settings = INSTANCE.get(context)
    }
}