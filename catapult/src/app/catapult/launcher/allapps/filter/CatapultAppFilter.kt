package app.catapult.launcher.allapps.filter

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.lifecycleScope
import app.catapult.launcher.CatapultLauncher
import app.catapult.launcher.launcher
import app.catapult.launcher.settings
import com.android.launcher3.AppFilter
import com.android.launcher3.BuildConfig

@Keep
class CatapultAppFilter(context: Context): AppFilter(context) {

    private val defaultHiddenApps = arrayOf(
        // Voice search
        ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/.VoiceSearchActivity"),
        // Wallpapers
        ComponentName.unflattenFromString("com.google.android.apps.wallpaper/.picker.CategoryPickerActivity"),
        // GNL
        ComponentName.unflattenFromString("com.google.android.launcher/.StubApp"),
        // Actions Services
        ComponentName.unflattenFromString("com.google.android.as/com.google.android.apps.miphone.aiai.allapps.main.MainDummyActivity"),
        // Lawnchair
        ComponentName(BuildConfig.APPLICATION_ID, CatapultLauncher::class.java.name),
    )

    override fun shouldShowApp(app: ComponentName?): Boolean {
        Log.d("CatapultAppFilter", "shouldShowApp - ${app?.flattenToString()}")
        if (defaultHiddenApps.contains(app)) {
            return false
        }
        return super.shouldShowApp(app)
    }
}