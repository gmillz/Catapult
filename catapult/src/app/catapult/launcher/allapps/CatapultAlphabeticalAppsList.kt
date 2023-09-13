package app.catapult.launcher.allapps

import android.content.Context
import androidx.lifecycle.lifecycleScope
import app.catapult.launcher.extensions.toComponentKey
import app.catapult.launcher.launcher
import app.catapult.launcher.settings.Settings
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.allapps.WorkProfileManager
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.views.ActivityContext
import java.util.function.Predicate

class CatapultAlphabeticalAppsList<T>(
    context: Context,
    appsStore: AllAppsStore?,
    workProfileManager: WorkProfileManager?
): AlphabeticalAppsList<T>(context, appsStore, workProfileManager) where T : Context?, T : ActivityContext? {

    private var hiddenApps = setOf<String>()
    private var itemFilter: Predicate<ItemInfo>? = null

    init {
        super.updateItemFilter { info ->
            require(info is AppInfo) { "`info` must be an instace of `AppInfo`." }
            when {
                itemFilter?.test(info)?: false -> false
                hiddenApps.contains(info.toComponentKey().toString()) -> false
                else -> true
            }
        }

        Settings.getInstance(context).hiddenApps.onEach(launcher.lifecycleScope) {
            hiddenApps = it
            onAppsUpdated()
        }
    }

    override fun updateItemFilter(itemFilter: Predicate<ItemInfo>?) {
        this.itemFilter = itemFilter
        onAppsUpdated()
    }
}