package app.catapult.launcher.data

import androidx.room.TypeConverter
import app.catapult.launcher.icons.IconPickerItem
import com.android.launcher3.util.ComponentKey

object Converters {

    @TypeConverter
    fun componentKeyToString(componentKey: ComponentKey) = componentKey.toString()

    @TypeConverter
    fun stringToComponentKey(data: String?) =
        if (data == null) null else ComponentKey.fromString(data)

    @TypeConverter
    fun iconPickerItemToString(item: IconPickerItem) = item.toString()

    @TypeConverter
    fun stringToIconPickerItem(string: String?) =
        if (string == null) null else IconPickerItem.fromString(string)

    @TypeConverter
    fun componentKeyListToString(keys: List<ComponentKey?>): String =
        keys.joinToString("|||") { it.toString() }

    @TypeConverter
    fun stringToComponentKeyList(string: String): List<ComponentKey?> =
        string.split("|||").map { ComponentKey.fromString(it) }
}
