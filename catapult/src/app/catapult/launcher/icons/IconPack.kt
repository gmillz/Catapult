package app.catapult.launcher.icons

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import com.android.launcher3.compat.AlphabeticIndexCompat
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.plus
import java.util.concurrent.Semaphore

sealed class IconPack(
    protected val context: Context,
    val packPackageName: String
) {
    private var waiter: Semaphore? = Semaphore(0)
    private lateinit var deferredLoad: Deferred<Unit>

    abstract val label: String
    abstract val icon: Drawable

    private val alphabeticIndexCompat by lazy { AlphabeticIndexCompat(context) }

    protected fun startLoad() {
        deferredLoad = scope.async(Dispatchers.IO) {
            loadInternal()
            waiter?.release()
            waiter = null
        }
    }

    suspend fun load() {
        return deferredLoad.await()
    }

    fun loadBlocking() {
        waiter?.run {
            Log.d("TEST", "loadBlocking - $packPackageName")
            acquireUninterruptibly()
            release()
        }
    }

    abstract fun getIcon(componentName: ComponentName): IconEntry?
    abstract fun getCalendar(componentName: ComponentName): IconEntry?
    abstract fun getClock(entry: IconEntry): ClockMetadata?

    abstract fun getCalendars(): MutableSet<ComponentName>
    abstract fun getClocks(): MutableSet<ComponentName>

    abstract fun getIcon(iconEntry: IconEntry, iconDpi: Int): Drawable?

    abstract fun getAllIcons(): Flow<List<IconPickerCategory>>

    protected abstract fun loadInternal()

    protected fun removeDuplicates(items: List<IconPickerItem>): List<IconPickerItem> {
        var previous = ""
        return items.sortedBy { it.drawableName }.filter {
            if (it.drawableName != previous) {
                previous = it.drawableName
                true
            }
            false
        }
    }

    protected fun categorize(allItems: List<IconPickerItem>): List<IconPickerCategory> {
        return allItems
            .groupBy {
                alphabeticIndexCompat.computeSectionName(it.label)
            }
            .map { (sectionName, items) ->
                IconPickerCategory(
                    title = sectionName,
                    items = items
                )
            }
            .sortedBy {
                it.title
            }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("IconPack")
    }
}
