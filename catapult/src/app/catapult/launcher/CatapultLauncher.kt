package app.catapult.launcher

import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import app.catapult.launcher.data.overrides.ItemOverride
import app.catapult.launcher.data.overrides.ItemOverrideRepository
import app.catapult.launcher.popup.CatapultShortcut
import app.catapult.launcher.updater.ApkUpdater
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherRootView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.ItemInfoWithIcon
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.util.ComponentKey
import com.android.systemui.plugins.shared.LauncherOverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.stream.Stream

class CatapultLauncher: Launcher(), LifecycleOwner, SavedStateRegistryOwner,
        ActivityResultRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    private val defaultOverlay by lazy { OverlayCallbackImpl(this) }

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry = savedStateRegistryController.savedStateRegistry

    override val lifecycle = lifecycleRegistry

    override val activityResultRegistry = object : ActivityResultRegistry() {
        override fun <I : Any?, O : Any?> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            val activity = this@CatapultLauncher

            // Immediate result path
            val synchronousResult = contract.getSynchronousResult(activity, input)
            if (synchronousResult != null) {
                Handler(Looper.getMainLooper()).post {
                    dispatchResult(
                        requestCode,
                        synchronousResult.value
                    )
                }
                return
            }

            // Start activity path
            val intent = contract.createIntent(activity, input)
            var optionsBundle: Bundle? = null
            // If there are any extras, we should defensively set the classLoader
            if (intent.extras != null && intent.extras!!.classLoader == null) {
                intent.setExtrasClassLoader(activity.classLoader)
            }
            if (intent.hasExtra(ActivityResultContracts.StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE)) {
                optionsBundle =
                    intent.getBundleExtra(ActivityResultContracts.StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE)
                intent.removeExtra(ActivityResultContracts.StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE)
            } else if (options != null) {
                optionsBundle = options.toBundle()
            }
            if (ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS == intent.action) {
                // requestPermissions path
                var permissions =
                    intent.getStringArrayExtra(ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS)
                if (permissions == null) {
                    permissions = arrayOfNulls(0)
                }
                ActivityCompat.requestPermissions(activity, permissions, requestCode)
            } else if (ActivityResultContracts.StartIntentSenderForResult.ACTION_INTENT_SENDER_REQUEST == intent.action) {
                val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(ActivityResultContracts.StartIntentSenderForResult.EXTRA_INTENT_SENDER_REQUEST, IntentSenderRequest::class.java)!!
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(
                        ActivityResultContracts.StartIntentSenderForResult.EXTRA_INTENT_SENDER_REQUEST
                    )!!
                }
                try {
                    // startIntentSenderForResult path
                    ActivityCompat.startIntentSenderForResult(
                        activity, request.intentSender,
                        requestCode, request.fillInIntent, request.flagsMask,
                        request.flagsValues, 0, optionsBundle
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Handler(Looper.getMainLooper()).post {
                        dispatchResult(
                            requestCode, RESULT_CANCELED,
                            Intent()
                                .setAction(ActivityResultContracts.StartIntentSenderForResult.ACTION_INTENT_SENDER_REQUEST)
                                .putExtra(
                                    ActivityResultContracts.StartIntentSenderForResult.EXTRA_SEND_INTENT_EXCEPTION, e)
                        )
                    }
                }
            } else {
                // startActivityForResult path
                ActivityCompat.startActivityForResult(activity, intent, requestCode, optionsBundle)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        savedStateRegistryController.performRestore(savedInstanceState)
        super.onCreate(savedInstanceState)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        launcher = this
    }

    override fun setupViews() {
        super.setupViews()
        val launcherRootView = findViewById<LauncherRootView>(R.id.launcher)
        //launcherRootView.setViewTreeLifecycleOwner(this)
        launcherRootView.setViewTreeSavedStateRegistryOwner(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savedStateRegistryController.performSave(outState)
    }

    fun getItemOverride(info: ItemInfoWithIcon): ItemOverride? {
        return runBlocking {
            ItemOverrideRepository.INSTANCE.get(this@CatapultLauncher)
                .get(ComponentKey(info.targetComponent, info.user), info.container)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        CoroutineScope(Dispatchers.IO).launch {
            val updater = ApkUpdater(this@CatapultLauncher, "https://github.com/gmillz/Catapult/releases/latest")
            if (updater.isInternetConnection()) {
                if (updater.isNewUpdateAvailable() == true) {
                    updater.requestDownload()
                }
            }
        }
    }

    override fun registerBackDispatcher() {
        if (Utilities.ATLEAST_T) {
            super.registerBackDispatcher()
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause() {
        super.onPause()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onStop() {
        super.onStop()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        launcher = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (activityResultRegistry.dispatchResult(requestCode, resultCode, data)) {
            mPendingActivityRequestCode = -1
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun getSupportedShortcuts(): Stream<SystemShortcut.Factory<*>> {
        return Stream.concat(
            super.getSupportedShortcuts(),
            Stream.of(CatapultShortcut.CUSTOMIZE)
        )
    }

    override fun getDefaultOverlay(): LauncherOverlayManager {
        return defaultOverlay
    }

    private fun restartIfPending() {
        when {
            restartFlags and FLAG_RESTART != 0 -> CatapultApp.app?.restart(false)
            restartFlags and FLAG_RECREATE != 0 -> {
                restartFlags = 0
                recreate()
            }
        }
    }

    private fun scheduleFlag(flag: Int) {
        restartFlags = restartFlags or flag
        if (lifecycleRegistry.currentState === Lifecycle.State.RESUMED) {
            restartIfPending()
        }
    }

    fun scheduleRecreate() {
        scheduleFlag(FLAG_RECREATE)
    }

    fun scheduleRestart() {
        scheduleFlag(FLAG_RESTART)
    }

    fun recreateIfNotScheduled() {
        if (restartFlags == 0) {
            LauncherAppState.getInstanceNoCreate().model.forceReload()
            LauncherAppState.getInstanceNoCreate().invariantDeviceProfile.onSettingsChanged(this)
            recreate()
        }
    }

    companion object {
        var launcher: CatapultLauncher? = null

        private const val FLAG_RECREATE = 1 shl 0
        private const val FLAG_RESTART = 1 shl 1

        var restartFlags = 0
    }

}

val launcher = CatapultLauncher.launcher!!
