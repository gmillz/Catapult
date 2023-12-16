package app.catapult.launcher.model

import android.content.ComponentName
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.launcher3.LauncherAppState
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.WorkspaceItemInfo

@Entity(
    tableName = "drawer_folder"
)
class DrawerFolder(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title: String,
    val content: List<ComponentName>
) {
    override fun equals(other: Any?): Boolean {
        if (other !is DrawerFolder) {
            return false
        }
        return id == other.id && title == other.title
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        return result
    }

    fun asFolderInfo(): DrawerFolderInfo {
        return DrawerFolderInfo(this)
    }
}