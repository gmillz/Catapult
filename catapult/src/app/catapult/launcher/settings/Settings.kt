package app.catapult.launcher.settings

import android.content.Context
import com.android.launcher3.util.MainThreadInitializedObject
import com.gmillz.compose.settings.BaseSettings

class Settings(context: Context): BaseSettings(context) {

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::Settings)
    }
}