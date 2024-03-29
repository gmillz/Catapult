package app.catapult.launcher

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import app.catapult.launcher.feed.FeedBridge
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.google.android.libraries.launcherclient.ISerializableScrollCallback
import com.google.android.libraries.launcherclient.LauncherClient
import com.google.android.libraries.launcherclient.LauncherClientCallbacks
import com.google.android.libraries.launcherclient.StaticInteger

class OverlayCallbackImpl(private val mLauncher: CatapultLauncher) : LauncherOverlayManager.LauncherOverlay,
    LauncherClientCallbacks, LauncherOverlayManager, ISerializableScrollCallback {
    private val mClient: LauncherClient
    private var mFlagsChanged = false
    private var mLauncherOverlayCallbacks: LauncherOverlayManager.LauncherOverlayCallbacks? = null
    private var mWasOverlayAttached = false
    private var mFlags = 0

    init {
        val enableFeed = settings.enableFeed.firstBlocking()
        mClient = LauncherClient(
            mLauncher, this, StaticInteger((if (enableFeed) 1 else 0) or 2 or 4 or 8)
        )
    }

    override fun onDeviceProvideChanged() {
        mClient.redraw()
    }

    override fun onAttachedToWindow() {
        mClient.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        mClient.onDetachedFromWindow()
    }

    override fun openOverlay() {
        mClient.showOverlay(true)
    }

    override fun hideOverlay(animate: Boolean) {
        mClient.hideOverlay(animate)
    }

    override fun hideOverlay(duration: Int) {
        mClient.hideOverlay(duration)
    }

    override fun startSearch(config: ByteArray?, extras: Bundle?) =
        mClient.startSearch(config, extras)

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) {
        mClient.onStart()
    }

    override fun onActivityResumed(activity: Activity) {
        mClient.onResume()
    }

    override fun onActivityPaused(activity: Activity) {
        mClient.onPause()
    }

    override fun onActivityStopped(activity: Activity) {
        mClient.onStop()
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        mClient.onDestroy()
        mClient.mDestroyed = true
    }

    override fun onOverlayScrollChanged(progress: Float) {
        mLauncherOverlayCallbacks?.onOverlayScrollChanged(progress)
    }

    override fun onServiceStateChanged(overlayAttached: Boolean, hotwordActive: Boolean) {
        onServiceStateChanged(overlayAttached)
    }

    override fun onServiceStateChanged(overlayAttached: Boolean) {
        if (overlayAttached != mWasOverlayAttached) {
            mWasOverlayAttached = overlayAttached
            mLauncher.setLauncherOverlay(if (overlayAttached) this else null)
        }
    }

    override fun onScrollInteractionBegin() {
        mClient.startScroll()
    }

    override fun onScrollInteractionEnd() {
        mClient.endScroll()
    }

    override fun onScrollChange(progress: Float, rtl: Boolean) {
        mClient.setScroll(progress)
    }

    override fun setOverlayCallbacks(callbacks: LauncherOverlayManager.LauncherOverlayCallbacks?) {
        mLauncherOverlayCallbacks = callbacks
    }

    override fun setPersistentFlags(flags: Int) {
        val newFlags = flags and (8 or 16)
        if (newFlags != mFlags) {
            mFlagsChanged = true
            mFlags = newFlags
            mLauncher.devicePrefs.edit().putInt(PREF_PERSIST_FLAGS, newFlags).apply()
        }
    }

    companion object {
        private const val PREF_PERSIST_FLAGS = "pref_persistent_flags"

        fun minusOneAvailable(context: Context): Boolean {
            return FeedBridge.useBridge(context) ||
                    context.applicationInfo.flags and
                    (ApplicationInfo.FLAG_DEBUGGABLE or ApplicationInfo.FLAG_SYSTEM) != 0
        }
    }
}