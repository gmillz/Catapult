package app.catapult.extensions

import android.content.Context
import android.service.notification.StatusBarNotification

private const val PERM_SUBSTITUTE_APP_NAME = "android.permission.SUBSTITUTE_NOTIFICATION_APP_NAME"
private const val EXTRA_SUBSTITUTE_APP_NAME = "android.substName"

fun StatusBarNotification.getAppName(context: Context): CharSequence {
    val subName = notification.extras.getString(EXTRA_SUBSTITUTE_APP_NAME)
    if (subName != null) {
        if (context.checkPackagePermission(packageName, PERM_SUBSTITUTE_APP_NAME)) {
            return subName
        }
    }
    return context.getAppName(packageName)
}