package app.catapult.launcher.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.catapult.launcher.launcher
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

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::Settings)

        @JvmStatic
        fun getInstance(context: Context): Settings = INSTANCE.get(context)
    }
}