package app.catapult.extensions

import android.content.pm.ApplicationInfo

val ApplicationInfo.isInstantApp: Boolean
    get() {
        val m = ApplicationInfo::class.java.getMethod("isInstantApp")
        return m.invoke(this) as Boolean
    }