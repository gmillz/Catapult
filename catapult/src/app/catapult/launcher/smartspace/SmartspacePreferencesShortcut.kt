package app.catapult.launcher.smartspace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.catapult.launcher.settings.Routes
import app.catapult.launcher.settings.SettingsActivity

class SmartspacePreferencesShortcut : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(SettingsActivity.getIntent(this, Routes.SMARTSPACE_WIDGET))
        finish()
    }
}
