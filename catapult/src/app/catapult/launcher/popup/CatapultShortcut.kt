package app.catapult.launcher.popup

import android.content.pm.LauncherActivityInfo
import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import app.catapult.launcher.CatapultLauncher
import app.catapult.launcher.views.ComposeBottomSheet
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.model.data.AppInfo as ModelAppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.util.ComponentKey

class CatapultShortcut {

    companion object {
        val CUSTOMIZE = SystemShortcut.Factory<CatapultLauncher> { activity, itemInfo, view ->
            Log.d("TEST", "item type - ${itemInfo.container}")
            getAppInfo(activity, itemInfo)?.let { Customize(activity, it, itemInfo, view) }
        }

        private fun getAppInfo(launcher: CatapultLauncher, itemInfo: ItemInfo): ModelAppInfo? {
            if (itemInfo is ModelAppInfo) return itemInfo
            if (itemInfo.itemType != ITEM_TYPE_APPLICATION) return null
            val key = ComponentKey(itemInfo.targetComponent, itemInfo.user)
            return launcher.appsView.appsStore.getApp(key)
        }
    }

    class Customize(
        private val launcher: CatapultLauncher,
        private val appInfo: ModelAppInfo,
        itemInfo: ItemInfo,
        view: View
    ): SystemShortcut<CatapultLauncher>(
        R.drawable.ic_edit,
        R.string.customize_button_text,
        launcher, itemInfo, view
    ) {
        override fun onClick(v: View?) {
            val outObj = Array<Any?>(1) { null }
            val icon = Utilities.getFullDrawable(launcher, appInfo, 0, 0, true, outObj)
            //if (mItemInfo.screenId != NO_ID && icon is BitmapInfo.Extender) {
            //    icon = icon
            //}
            val launcherActivityInfo = outObj[0] as LauncherActivityInfo
            val defaultTitle = launcherActivityInfo.label.toString()

            AbstractFloatingView.closeAllOpenViews(launcher)
            ComposeBottomSheet.show(
                context = launcher,
                contentPaddings = PaddingValues(bottom = 64.dp)
            ) {
                CustomizeAppDialog(
                    icon = icon,
                    defaultTitle = defaultTitle,
                    componentKey = ComponentKey(appInfo.componentName, appInfo.user),
                    container = mItemInfo.container,
                    onClose = { close(true) }
                )
            }
        }
    }
}
