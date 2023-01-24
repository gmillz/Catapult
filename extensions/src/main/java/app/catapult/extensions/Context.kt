package app.catapult.extensions

import android.content.Context
import android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED
import android.content.pm.PackageManager

import androidx.core.content.getSystemService
import java.util.Locale

val Context.locale: Locale?
    get() = resources.configuration.locales[0]

fun Context.isThemedIconsEnabled() = true
inline fun <reified T : Any> Context.requireSystemService(): T = checkNotNull(getSystemService())

fun Context.getAppName(packageName: String): CharSequence {
    try {
        return packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA))
    } catch (_: PackageManager.NameNotFoundException) {}
    return packageName
}

fun Context.checkPackagePermission(packageName: String, permissionName: String): Boolean {
    try {
        val info = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        info.requestedPermissions.forEachIndexed { index, s ->
            if (s == permissionName) {
                return info.requestedPermissionsFlags[index].hasFlag(REQUESTED_PERMISSION_GRANTED)
            }
        }
    } catch (_: PackageManager.NameNotFoundException) {
    }
    return false
}