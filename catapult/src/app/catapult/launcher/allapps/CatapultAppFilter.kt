package app.catapult.launcher.allapps

import android.content.ComponentName
import android.content.Context
import app.catapult.launcher.CatapultLauncher
import com.android.launcher3.AppFilter

class CatapultAppFilter(
    context: Context
): AppFilter(context) {

    private val hideList = HashSet<ComponentName>()


    init {
        hideList.add(ComponentName(context, CatapultLauncher::class.java))
    }

    override fun shouldShowApp(app: ComponentName?): Boolean {
        return !hideList.contains(app)
                && super.shouldShowApp(app)
    }
}