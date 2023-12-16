package app.catapult.launcher.model

import com.android.launcher3.LauncherAppState
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.WorkspaceItemInfo

class DrawerFolderInfo(val folder: DrawerFolder): FolderInfo() {

    init {
        title = folder.title
        for (item in folder.content) {
            val info = LauncherAppState.getInstanceNoCreate().model.getAppInfoForComponent(item)
            if (info != null) {
                add(WorkspaceItemInfo(info), false)
            }
        }
    }
}