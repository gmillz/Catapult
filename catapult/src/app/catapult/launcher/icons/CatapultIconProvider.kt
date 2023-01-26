package app.catapult.launcher.icons

import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.Resources.ID_NULL
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import androidx.core.os.BuildCompat
import app.catapult.launcher.data.overrides.ItemOverrideRepository
import app.catapult.launcher.settings
import app.catapult.launcher.util.MultiSafeCloseable
import app.catapult.launcher.util.dropWhileBusy
import app.catapult.launcher.util.subscribeBlocking
import com.android.launcher3.icons.ClockDrawableWrapper
import com.android.launcher3.icons.LauncherIconProvider
import com.android.launcher3.util.SafeCloseable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.function.Supplier


@BuildCompat.PrereleaseSdkCheck class CatapultIconProvider @JvmOverloads constructor(
    private val context: Context,
    private val supportsIconTheme: Boolean = false
): LauncherIconProvider(context) {

    private val iconPackProvider = IconPackProvider.INSTANCE.get(context)
    private val iconPack get() = iconPackProvider.getIconPack(settings.iconPack.firstBlocking())?.apply { loadBlocking() }
    private val itemOverrideRepo = ItemOverrideRepository.INSTANCE.get(context)

    override fun registerIconChangeListener(
        listener: IconChangeListener,
        handler: Handler
    ): SafeCloseable {
        return MultiSafeCloseable().apply {
            add(super.registerIconChangeListener(listener, handler))
            add(IconPackChangeReceiver(context, handler, listener))
        }
    }

    private inner class IconPackChangeReceiver(
        private val context: Context,
        private val handler: Handler,
        private val listener: IconChangeListener
    ): SafeCloseable {

        private var iconState = systemIconState

        init {
            settings.iconPack.get().dropWhileBusy()
                .subscribeBlocking(scope = CoroutineScope(Dispatchers.Main)) {
                    val newState = systemIconState
                    if (iconState != newState) {
                        iconState = newState
                        listener.onSystemIconStateChanged(iconState)
                    }
                }
        }

        override fun close() {}
    }

    override fun getIcon(info: LauncherActivityInfo, iconDpi: Int): Drawable {
        return getIconWithOverrides(
            info.componentName.packageName,
            info.name,
            iconDpi,
        ) {
            info.getIcon(iconDpi)
        }
    }

    override fun getIcon(info: ActivityInfo): Drawable {
        return getIcon(info, context.resources.configuration.densityDpi)

    }

    override fun getIcon(info: ActivityInfo, iconDpi: Int): Drawable {
        return getIconWithOverrides(
            info.applicationInfo.packageName,
            info.name,
            iconDpi
        ) {
            loadActivityInfoIcon(info, iconDpi)
        }
    }

    override fun getIconWithOverrides(
        packageName: String,
        component: String,
        iconDpi: Int,
        fallback: Supplier<Drawable>): Drawable {

        val componentName = ComponentName(packageName, component)
        val iconEntry = resolveIconEntry(componentName)
        var resolvedEntry = iconEntry
        if (iconEntry != null) {
            if (iconEntry.type == IconType.Calendar) {
                resolvedEntry = iconEntry.resolveDynamicCalendar(getDay())
            }

            val icon = resolvedEntry?.let { iconPackProvider.getDrawable(it, iconDpi) }
            if (icon != null) {
                return icon
            }
        } else {
            if (isDynamicClockPackage(componentName)) {
                return ClockDrawableWrapper.forPackage(context, packageName, iconDpi, getThemeDataForPackage(packageName))
            }
            if (isDynamicCalendarPackage(componentName)) {
                val d = loadCalendarDrawable(componentName, iconDpi)
                if (d != null) return d
            }
        }

        return super.getIconWithOverrides(packageName, component, iconDpi, fallback)
    }

    private fun loadCalendarDrawable(componentName: ComponentName, iconDpi: Int): Drawable? {
        val pm = context.packageManager
        try {
            val metadata = pm.getActivityInfo(componentName,
            PackageManager.GET_UNINSTALLED_PACKAGES or PackageManager.GET_META_DATA)
                .metaData

            val resources = pm.getResourcesForApplication(componentName.packageName)
            val id = getDynamicIconId(componentName.packageName, metadata, resources)
            if (id != ID_NULL) {
                val drawable = resources.getDrawableForDensity(id, iconDpi, null)
                return drawable
            }
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        }
        return null
    }

    private fun getDynamicIconId(packageName: String, metadata: Bundle?, resources: Resources): Int {
        if (metadata == null) {
            return ID_NULL
        }
        val key = "$packageName.dynamic_icons"
        val arrayId = metadata.getInt(key, ID_NULL)
        if (arrayId == ID_NULL) {
            return ID_NULL
        }
        return try {
            resources.obtainTypedArray(arrayId).getResourceId(getDay(), ID_NULL)
        } catch (e: Resources.NotFoundException) {
            ID_NULL
        }
    }

    private fun isDynamicCalendarPackage(componentName: ComponentName): Boolean {
        val activityInfo = context.packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA)
        val meta = activityInfo.metaData ?: return false
        return meta.containsKey("${componentName.packageName}.dynamic_icons")
    }

    private fun isDynamicClockPackage(componentName: ComponentName): Boolean {
        val appInfo: ApplicationInfo = context.packageManager.getApplicationInfo(
            componentName.packageName,
            PackageManager.MATCH_UNINSTALLED_PACKAGES or PackageManager.GET_META_DATA
        )
        val meta = appInfo.metaData ?: return false
        return meta.containsKey(ClockDrawableWrapper.DEFAULT_HOUR_METADATA_KEY) &&
            meta.containsKey(ClockDrawableWrapper.DEFAULT_MINUTE_METADATA_KEY) &&
            meta.containsKey(ClockDrawableWrapper.DEFAULT_SECOND_METADATA_KEY) &&
            meta.containsKey(ClockDrawableWrapper.HOUR_INDEX_METADATA_KEY) &&
            meta.containsKey(ClockDrawableWrapper.MINUTE_INDEX_METADATA_KEY) &&
            meta.containsKey(ClockDrawableWrapper.SECOND_INDEX_METADATA_KEY) &&
            meta.containsKey(ClockDrawableWrapper.ROUND_ICON_METADATA_KEY)
    }

    private fun resolveIconEntry(componentName: ComponentName): IconEntry? {
        /*val componentKey = ComponentKey(componentName, user)
        val itemOverride = runBlocking {
            itemOverrideRepo.get(componentKey)
        }
        if (itemOverride?.iconPickerItem != null) {
            return itemOverride.iconPickerItem!!.toIconEntry()
        }*/

        val iconPack = this.iconPack?: return null
        val calendarEntry = iconPack.getCalendar(componentName)
        if (calendarEntry != null) {
            return calendarEntry
        }

        return iconPack.getIcon(componentName)
    }

    private fun loadActivityInfoIcon(info: ActivityInfo, iconDpi: Int): Drawable {
        val iconRes = info.iconResource
        var icon: Drawable? = null
        if (iconDpi != 0 && iconRes != 0) {
            try {
                val resources = context.packageManager
                    .getResourcesForApplication(info.applicationInfo)
                icon = resources.getDrawableForDensity(iconRes, iconDpi, null)
            } catch (_: Exception) {}
        }
        if (icon == null) {
            icon = info.loadIcon(context.packageManager)
        }
        return icon!!
    }

    override fun getSystemIconState(): String {
        return super.getSystemIconState() + ",pack:${iconPack}"
    }
}
