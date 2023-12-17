package app.catapult.launcher.data

import android.content.ComponentName
import androidx.room.TypeConverter
import app.catapult.launcher.icons.IconPickerItem
import com.android.launcher3.util.ComponentKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

    @TypeConverter
    fun componentNameListToString(list: List<ComponentName>): String = Gson().toJson(list.distinct().map { it.flattenToString() })

    @TypeConverter
    fun stringToComponentNameList(data: String?): List<ComponentName> {
        if (data == null || data == "") return listOf()

        val listType = object: TypeToken<List<String>>() {}
        return Gson().fromJson(data, listType).map { ComponentName.unflattenFromString(it)!! }.distinct()
    }
}
