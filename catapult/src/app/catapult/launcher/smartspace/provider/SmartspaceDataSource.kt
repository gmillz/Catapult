package app.catapult.launcher.smartspace.provider

import android.app.Activity
import android.content.Context
import app.catapult.launcher.settings
import app.catapult.launcher.settings.Settings
import app.catapult.launcher.smartspace.model.SmartspaceTarget
import com.gmillz.compose.settings.SettingEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

sealed class SmartspaceDataSource(
    val context: Context,
    val providerName: Int,
    getEnabledSetting: Settings.() -> SettingEntry<Boolean, Boolean>
) {
    val enabledSetting = getEnabledSetting(settings)
    open val isAvailable: Boolean = true

    protected abstract val internalTargets: Flow<List<SmartspaceTarget>>
    open val disabledTargets: List<SmartspaceTarget> = emptyList()

    private val restartSignal = MutableStateFlow(0)
    private val enabledTargets get() = internalTargets
        .onStart {
            if (requiresSetup()) throw RequiresSetupException()
        }
        .map { State(targets = it) }
        .catch {
            if (it is RequiresSetupException) {
                emit(
                    State(
                    targets = disabledTargets,
                    requiresSetup = listOf(this@SmartspaceDataSource))
                )
            } else {
                enabledSetting.set(false)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val targets = enabledSetting.get()
        .distinctUntilChanged()
        .flatMapLatest { isEnabled ->
            if (isAvailable && isEnabled)
                restartSignal.flatMapLatest { enabledTargets }
            else
                flowOf(State(targets = disabledTargets))
        }

    open suspend fun requiresSetup(): Boolean = false

    open suspend fun startSetup(activity: Activity) {}

    suspend fun onSetupDone() {
        if (!requiresSetup()) {
            restart()
        } else {
            enabledSetting.set(false)
        }
    }

    private fun restart() {
        restartSignal.value++
    }

    private class RequiresSetupException : RuntimeException()

    data class State(
        val targets: List<SmartspaceTarget> = emptyList(),
        val requiresSetup: List<SmartspaceDataSource> = emptyList()
    ) {
        operator fun plus(other: State): State {
            return State(
                targets = this.targets + other.targets,
                requiresSetup = this.requiresSetup + other.requiresSetup
            )
        }
    }
}
