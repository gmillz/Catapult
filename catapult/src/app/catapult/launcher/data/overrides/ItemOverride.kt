package app.catapult.launcher.data.overrides

import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.catapult.launcher.icons.IconPackProvider
import app.catapult.launcher.icons.IconPickerItem
import app.catapult.launcher.launcher
import com.android.launcher3.LauncherSettings
import com.android.launcher3.icons.FastBitmapDrawable
import com.android.launcher3.model.data.ItemInfoWithIcon
import com.android.launcher3.util.ComponentKey

@Entity(
    tableName = "item_overrides"
)
data class ItemOverride(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val componentKey: ComponentKey,
    var overrideTitle: String? = null,
    var iconPickerItem: IconPickerItem? = null,
    val container: Int = LauncherSettings.Favorites.CONTAINER_UNKNOWN,
) {
    fun newIcon(info: ItemInfoWithIcon): FastBitmapDrawable {
        if (info.container != container) {
            return info.newIcon(launcher)
        }
        if (iconPickerItem != null) {
            val drawable = IconPackProvider.INSTANCE.noCreate.getDrawable(iconPickerItem!!.toIconEntry(), 0, componentKey.user)
            if (drawable != null) {
                return FastBitmapDrawable(drawable.toBitmap())
            }
        }
        return info.newIcon(launcher)
    }

    fun getLabel(info: ItemInfoWithIcon): CharSequence {
        if (overrideTitle != null) return overrideTitle!!
        return info.title ?: ""
    }
}
