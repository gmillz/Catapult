package app.catapult.launcher.data.overrides

import android.content.Context
import app.catapult.launcher.data.AppDatabase
import app.catapult.launcher.icons.IconPickerItem
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.MainThreadInitializedObject
import kotlinx.coroutines.flow.Flow

class ItemOverrideRepository(context: Context) {

    private val dao = AppDatabase.INSTANCE.get(context).itemOverrideDao()

    suspend fun get(componentKey: ComponentKey, container: Int): ItemOverride? {
        return dao.get(componentKey, container)
    }

    fun getAll(): Flow<List<ItemOverride>> {
        return dao.getAll()
    }

    suspend fun put(item: ItemOverride) {
        if (item.overrideTitle == null && item.iconPickerItem == null) {
            return
        }
        dao.insert(item)
    }

    suspend fun delete(item: ItemOverride) {
        dao.delete(item)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun putOverrideIcon(componentKey: ComponentKey, container: Int, item: IconPickerItem) {
        var itemOverride = get(componentKey, container)
        if (itemOverride == null) {
            itemOverride = ItemOverride(0, componentKey, "", item)
        } else {
            itemOverride.iconPickerItem = item
        }
        put(itemOverride)
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::ItemOverrideRepository)
    }
}
