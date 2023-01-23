package app.catapult.launcher.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import app.catapult.launcher.launcher
import com.android.launcher3.util.MainThreadInitializedObject
import com.gmillz.compose.settings.BaseSettings

class Settings(context: Context): BaseSettings(context) {

    val recreate = { launcher.recreateIfNotScheduled() }

    val dockSearchBarEnabled = setting(
        key = booleanPreferencesKey("dock_search_bar_enabled"),
        defaultValue = true,
        onSet = { recreate() }
    )
    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::Settings)

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE.get(context)
    }
}