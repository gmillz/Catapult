package app.catapult.launcher.settings.ui.components

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.os.Handler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.catapult.launcher.allapps.CatapultAppFilter
import com.android.launcher3.AppFilter
import com.android.launcher3.LauncherAppState
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MODEL_EXECUTOR

@Composable
fun appsList(
    filter: AppFilter = CatapultAppFilter(LocalContext.current),
    comparator: Comparator<App> = defaultComparator
): State<List<App>> {
    val context = LocalContext.current
    val appsState = remember { mutableStateOf(emptyList<App>()) }

    DisposableEffect(Unit) {
        Utilities.postAsyncCallback(Handler(MODEL_EXECUTOR.looper)) {
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            appsState.value = UserCache.INSTANCE.get(context).userProfiles.asSequence()
                .flatMap { launcherApps.getActivityList(null, it) }
                .filter { filter.shouldShowApp(it.componentName) }
                .map { App(context, it) }
                .sortedWith(comparator)
                .toList()
        }

        onDispose {  }
    }
    return appsState
}

class App(context: Context, val info: LauncherActivityInfo) {
    val label get() = info.label.toString()
    val icon: Bitmap
    val key = ComponentKey(info.componentName, info.user)

    init {
        val appInfo = AppInfo(context, info, info.user)
        LauncherAppState.getInstanceNoCreate().iconCache.getTitleAndIcon(appInfo, false)
        icon = appInfo.bitmap.icon
    }
}

private val defaultComparator = Comparator.comparing<App, String> {
    it.info.label.toString().lowercase()
}
