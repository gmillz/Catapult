package app.catapult.launcher.icons

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.Resources.ID_NULL
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.util.Log
import androidx.annotation.RequiresApi
import app.catapult.extensions.getThemedIconPacksInstalled
import app.catapult.extensions.isThemedIconsEnabled
import com.android.launcher3.icons.ClockDrawableWrapper
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.icons.ThemedIconDrawable
import com.android.launcher3.util.MainThreadInitializedObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class IconPackProvider(private val context: Context) {

    private val systemIconPack = SystemIconPack(context)
    private val iconPacks = mutableMapOf<String, IconPack?>()

    fun getIconPackOrSystem(packageName: String): IconPack? {
        if (packageName.isEmpty() || packageName == "system") return systemIconPack
        return getIconPack(packageName)
    }

    fun getIconPack(packageName: String): IconPack? {
        if (packageName.isEmpty()) {
            return null
        }
        if (packageName == "system") {
            return iconPacks.getOrPut(packageName) {
                SystemIconPack(context)
            }
        }
        return iconPacks.getOrPut(packageName) {
            try {
                CustomIconPack(
                    context,
                    packageName,
                    context.packageManager.let { pm ->
                        pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString()
                    }
                )
            } catch(e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                null
            }
        }
    }

    //fun getClockMetadata(iconEntry: IconEntry): ClockMetadata? {
    //    val iconPack = getIconPackOrSystem(iconEntry.packPackageName)?: return null
    //    return iconPack.getClock(iconEntry)
    //}

    fun getDrawable(iconEntry: IconEntry, iconDpi: Int): Drawable? {
        return getDrawable(iconEntry, iconDpi, Process.myUserHandle())
    }

    fun getDrawable(iconEntry: IconEntry, iconDpi: Int, user: UserHandle): Drawable? {
        val iconPack = getIconPackOrSystem(iconEntry.packPackageName) ?: return null
        iconPack.loadBlocking()
        val packageManager =  context.packageManager
        val drawable = iconPack.getIcon(iconEntry, iconDpi) ?: return null
        val themedIconPacks = packageManager.getThemedIconPacksInstalled(context)
        if (
            context.isThemedIconsEnabled() && iconEntry.packPackageName in themedIconPacks
        ) {
            val themedColors: IntArray = ThemedIconDrawable.getColors(context)
            val res = packageManager.getResourcesForApplication(iconEntry.packPackageName)
            @SuppressLint("DiscouragedApi")
            val resId = res.getIdentifier(iconEntry.name, "drawable", iconEntry.packPackageName)
            val bg: Drawable = ColorDrawable(themedColors[0])
            //val td = ThemeData(res, iconEntry.packPackageName, resId)
            val fg = drawable
            return if (fg is AdaptiveIconDrawable) {
                val foregroundDr = fg.foreground.apply { setTint(themedColors[1]) }
                AdaptiveIconDrawable(bg, foregroundDr)
            } else {
                val iconFromPack = InsetDrawable(drawable, .3f).apply { setTint(themedColors[1]) }
                //td.wrapDrawable(AdaptiveIconDrawable(bg, iconFromPack), 0)
                AdaptiveIconDrawable(bg, fg)
            }
        }
        val clockMetadata = if (user == Process.myUserHandle()) iconPack.getClock(iconEntry) else null
        if (clockMetadata != null) {
            val clockDrawable = ClockDrawableWrapper.forMeta(clockMetadata) {
                drawable
            }
            if (clockDrawable != null) {
                return clockDrawable
            }
        }
        return drawable
    }

    fun getIconPacks(): Flow<List<IconPack>> = flow {
        val iconPacks = iconPackIntents
            .flatMap {
                context.applicationContext.packageManager.queryIntentActivities(it, 0)
            }
            .associateBy {
                it.activityInfo.packageName
            }
            .mapTo(mutableListOf()) { (_, info) ->
                CustomIconPack(
                    context,
                    info.activityInfo.packageName,
                    info.activityInfo.loadLabel(context.packageManager).toString()
                )
            }
        val defaultIconPack = SystemIconPack(context)
        emit(listOf(defaultIconPack) + iconPacks.sortedBy { it.label })
    }.flowOn(Dispatchers.IO)

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::IconPackProvider)

        val iconPackIntents = listOf(
            Intent("com.novalauncher.THEME"),
            Intent("org.adw.launcher.icons.ACTION_PICK_ICON"),
            Intent("com.dlto.atom.launcher.THEME"),
            Intent("android.intent.action.MAIN").addCategory("com.anddoes.launcher.THEME")
        )
    }
}
