package app.catapult.launcher.data.drawer

import android.content.Context
import app.catapult.launcher.data.AppDatabase
import app.catapult.launcher.model.DrawerFolder
import com.android.launcher3.util.MainThreadInitializedObject
import kotlinx.coroutines.flow.Flow

class DrawerFolderRepository(context: Context) {
    private val dao = AppDatabase.INSTANCE.get(context).drawerFolderDao()

    suspend fun getFolder(id: Int): DrawerFolder {
        return dao.get(id)
    }

    fun getFolderFlow(id: Int): Flow<DrawerFolder> {
        return dao.getAsFlow(id)
    }

    fun getFolders(): Flow<List<DrawerFolder>> {
        return dao.getAll()
    }

    suspend fun putFolder(folder: DrawerFolder): Int {
        return dao.insert(folder).toInt()
    }

    suspend fun deleteFolder(folder: DrawerFolder) {
        dao.delete(folder)
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::DrawerFolderRepository)
    }
}