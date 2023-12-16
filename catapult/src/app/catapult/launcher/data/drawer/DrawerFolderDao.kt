package app.catapult.launcher.data.drawer

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.catapult.launcher.model.DrawerFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawerFolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DrawerFolder): Long

    @Delete
    suspend fun delete(item: DrawerFolder)

    @Query("SELECT * from drawer_folder")
    fun getAll(): Flow<List<DrawerFolder>>

    @Query("SELECT * from drawer_folder WHERE id is :id")
    suspend fun get(id: Int): DrawerFolder

    @Query("SELECT * from drawer_folder WHERE id is :id")
    fun getAsFlow(id: Int): Flow<DrawerFolder>
}