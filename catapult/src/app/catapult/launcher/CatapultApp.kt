package app.catapult.launcher

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import app.catapult.extensions.requireSystemService
import app.catapult.launcher.settings.Settings
import kotlin.system.exitProcess

class CatapultApp: Application() {

    override fun onCreate() {
        app = this
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
        app = null
    }

    fun restart(recreateLauncher: Boolean = true) {
        if (recreateLauncher) {
            // TODO
        } else {
            restartLauncher()
        }
    }

    private fun restartLauncher() {
        val pm = packageManager
        var intent: Intent? = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val componentName = intent!!.resolveActivity(pm)
        if (packageName != componentName.packageName) {
            intent = pm.getLaunchIntentForPackage(packageName)
                ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        restartLauncher(intent)
    }

    private fun restartLauncher(intent: Intent?) {
        startActivity(intent)

        // Create a pending intent so the application is restarted after System.exit(0) was called.
        // We use an AlarmManager to call this intent in 100ms
        val mPendingIntent =
            PendingIntent.getActivity(this, 0, intent, FLAG_CANCEL_CURRENT or FLAG_IMMUTABLE)
        val mgr: AlarmManager = requireSystemService()
        mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent

        // Kill the application
        killLauncher()
    }

    private fun killLauncher() {
        exitProcess(0)
    }


    companion object {
        var app: CatapultApp? = null
    }
}

@SuppressLint("StaticFieldLeak")
val settings: Settings = Settings.INSTANCE.get(CatapultApp.app!!)