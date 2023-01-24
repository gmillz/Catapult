package app.catapult.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

@Suppress("DEPRECATION")
fun PackageManager.getThemedIconPacksInstalled(context: Context): List<String> =
    try {
        queryIntentActivityOptions(
            ComponentName(context.applicationInfo.packageName, context.applicationInfo.className),
            null,
            Intent("app.lawnchair.icons.THEMED_ICON"),
            PackageManager.GET_RESOLVED_FILTER
        ).map { it.activityInfo.packageName }
    } catch (_: PackageManager.NameNotFoundException) {
        emptyList()
    }

fun PackageManager.isPackageInstalledAndEnabled(packageName: String) = try {
    getApplicationInfo(packageName, 0).enabled
} catch (_: PackageManager.NameNotFoundException) {
    false
}