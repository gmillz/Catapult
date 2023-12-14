package app.catapult.launcher

import android.content.Context
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.InvariantDeviceProfile.GridOption
import com.android.launcher3.util.MainThreadInitializedObject

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