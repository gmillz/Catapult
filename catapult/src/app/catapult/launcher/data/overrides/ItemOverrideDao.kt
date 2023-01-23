package app.catapult.launcher.data.overrides

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.launcher3.util.ComponentKey
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemOverrideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemOverride)

    @Delete
    suspend fun delete(item: ItemOverride)

    @Query("DELETE FROM item_overrides")
    suspend fun deleteAll()

    @Query("SELECT * from item_overrides")
    fun getAll(): Flow<List<ItemOverride>>

    @Query("SELECT * from item_overrides WHERE componentKey is :key")
    suspend fun get(key: ComponentKey): ItemOverride?

    @Query("SELECT * from item_overrides WHERE componentKey is :key AND container is :container")
    suspend fun get(key: ComponentKey, container: Int): ItemOverride?
}
