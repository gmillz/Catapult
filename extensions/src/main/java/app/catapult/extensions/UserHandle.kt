package app.catapult.extensions

import android.os.UserHandle

val UserHandle.identifier: Int
    get() {
        val m = UserHandle::class.java.getMethod("getIdentifier")
        return m.invoke(this) as Int
    }