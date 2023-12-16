package app.catapult.launcher.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import app.catapult.launcher.data.drawer.DrawerFolderDao
import app.catapult.launcher.data.overrides.ItemOverride
import app.catapult.launcher.data.overrides.ItemOverrideDao
import app.catapult.launcher.model.DrawerFolder
import com.android.launcher3.util.MainThreadInitializedObject

@Database(
    version = 2,
    entities = [ItemOverride::class, DrawerFolder::class],
    autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ])
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun itemOverrideDao(): ItemOverrideDao

    abstract fun drawerFolderDao(): DrawerFolderDao

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject { context ->
            Room.databaseBuilder(
                context,
                AppDatabase::class.java, "settings"
            ).build()
        }
    }
}
