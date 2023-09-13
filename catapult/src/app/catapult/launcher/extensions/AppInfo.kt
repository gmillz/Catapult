package app.catapult.launcher.extensions

import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.ComponentKey

fun AppInfo.toComponentKey(): ComponentKey {
    return ComponentKey(componentName, user)
}