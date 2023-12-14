package app.catapult.launcher

import android.content.Context
import com.android.launcher3.DeviceProfile
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.InvariantDeviceProfile.GridOption
import com.android.launcher3.InvariantDeviceProfile.INDEX_DEFAULT
import com.android.launcher3.InvariantDeviceProfile.INDEX_LANDSCAPE
import com.android.launcher3.InvariantDeviceProfile.INDEX_TWO_PANEL_LANDSCAPE
import com.android.launcher3.InvariantDeviceProfile.INDEX_TWO_PANEL_PORTRAIT
import com.android.launcher3.Utilities
import com.android.launcher3.util.MainThreadInitializedObject
import kotlin.time.times

class DeviceProfileOverrides(context: Context) {

    private val predefinedGrids = InvariantDeviceProfile.parseAllGridOptions(context)
        .map { option ->
            val gridInfo = DbGridInfo(
                numHotseatColumns = option.numHotseatIcons,
                numRows = option.numRows,
                numColumns = option.numColumns
            )
            gridInfo to option.name
        }

    fun getGridInfo() = DbGridInfo()

    fun getGridInfo(name: String) = predefinedGrids
        .first { it.second == name }
        .first

    fun getGridName(gridInfo: DbGridInfo): String {
        val match = predefinedGrids
            .firstOrNull { it.first.numRows >= gridInfo.numRows && it.first.numColumns >= gridInfo.numColumns }
            ?: predefinedGrids.last()
        return match.second
    }

    fun setDefaultsIfNeeded(gridOption: GridOption): Boolean {
        var res = false
        if (settings.hotseatColumns.firstBlocking() == -1) {
            settings.hotseatColumns.set(gridOption.numHotseatIcons)
            res = true
        }
        if (settings.workspaceRows.firstBlocking() == -1) {
            settings.workspaceRows.set(gridOption.numRows)
            res = true
        }
        if (settings.workspaceColumns.firstBlocking() == -1) {
            settings.workspaceColumns.set(gridOption.numColumns)
            res = true
        }
        return res
    }

    fun applyDeviceProfileIconOverrides(dp: DeviceProfile) {

        val enableIconText = settings.showIconLabelsOnHomescreen.firstBlocking()
        val iconTextSizeFactor = if (enableIconText) 1f else 0f

        val enableDrawerIconText = settings.showIconLabelsInDrawer.firstBlocking()
        val drawerIconTextSizeFactor = if (enableDrawerIconText) 1f else 0f

        dp.iconTextSizePx = (iconTextSizeFactor * dp.iconTextSizePx).toInt()
        val allAppsIconTextSizePx = dp.allAppsIconTextSizePx
        dp.allAppsIconTextSizePx *= drawerIconTextSizeFactor
        if (!enableDrawerIconText) {
            dp.allAppsCellHeightPx -= allAppsIconTextSizePx.toInt()
        }
    }

    fun applyInvariantDeviceProfileOverrides(idp: InvariantDeviceProfile) {
        val iconSizeFactor = settings.iconSizeFactorHomescreen.firstBlocking()
        idp.iconSize[INDEX_DEFAULT] *= iconSizeFactor
        idp.iconSize[INDEX_LANDSCAPE] *= iconSizeFactor
        idp.iconSize[INDEX_TWO_PANEL_PORTRAIT] *= iconSizeFactor
        idp.iconSize[INDEX_TWO_PANEL_LANDSCAPE] *= iconSizeFactor
    }

    data class DbGridInfo(
        val numHotseatColumns: Int,
        val numRows: Int,
        val numColumns: Int,
    ) {
        val dbFile get() = "launcher_${numRows}_${numColumns}_$numHotseatColumns.db"

        constructor(): this(
            numHotseatColumns = settings.hotseatColumns.firstBlocking(),
            numRows = settings.workspaceRows.firstBlocking(),
            numColumns = settings.workspaceColumns.firstBlocking()
        )
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::DeviceProfileOverrides)
    }
}