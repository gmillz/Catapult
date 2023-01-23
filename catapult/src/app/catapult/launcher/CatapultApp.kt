package app.catapult.launcher

import android.annotation.SuppressLint
import android.app.Application
import app.catapult.launcher.settings.Settings

class CatapultApp: Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
    }

    override fun onTerminate() {
        super.onTerminate()
        app = null
    }

    companion object {
        var app: CatapultApp? = null
    }
}

@SuppressLint("StaticFieldLeak")
val settings: Settings = Settings.INSTANCE.get(CatapultApp.app!!)