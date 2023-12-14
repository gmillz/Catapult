package app.catapult.launcher.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.catapult.launcher.data.overrides.ItemOverride
import app.catapult.launcher.data.overrides.ItemOverrideDao
import com.android.launcher3.util.MainThreadInitializedObject

@Database(entities = [ItemOverride::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun itemOverrideDao(): ItemOverrideDao

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
