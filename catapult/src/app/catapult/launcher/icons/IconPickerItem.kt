package app.catapult.launcher.icons

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

private const val SEPARATOR = "|||"

@Parcelize
data class IconPickerItem(
    val packPackageName: String,
    val drawableName: String,
    val label: String,
    val type: IconType
) : Parcelable {
    fun toIconEntry() = IconEntry(
        packPackageName = packPackageName,
        name = drawableName,
        type = type
    )

    override fun toString(): String {
        return packPackageName + SEPARATOR +
            drawableName + SEPARATOR +
            label + SEPARATOR +
            type + SEPARATOR
    }

    companion object {
        fun fromString(string: String): IconPickerItem {
            val a = string.split(SEPARATOR)
            return IconPickerItem(
                packPackageName = a[0],
                drawableName = a[1],
                label = a[2],
                type = IconType.Normal
            )
        }
    }
}
