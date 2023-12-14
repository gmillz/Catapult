package app.catapult.launcher.feed

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.util.Log
import app.catapult.launcher.settings
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.MainThreadInitializedObject

class FeedBridge(private val context: Context) {

    private val shouldUseFeed = context.applicationInfo.flags and (FLAG_DEBUGGABLE or FLAG_SYSTEM) == 0
    val bridgePackages = mutableListOf<BridgeInfo>().apply {
        add(PixelBridgeInfo("com.google.android.apps.nexuslauncher", R.integer.bridge_signature_hash))
        add(BridgeInfo("app.lawnchair.lawnfeed", R.integer.lawnfeed_signature_hash))
        add(CustomBridgeInfo("com.google.android.googlequicksearchbox"))

        whitelist.forEach {
            add(CustomBridgeInfo(it.key))
        }
    }

    fun resolveBridge(): BridgeInfo? {
        val customBridge = customBridgeOrNull()
        return when {
            customBridge != null -> customBridge
            !shouldUseFeed -> null
            else -> bridgePackages.firstOrNull { it.isAvailable() }
        }
    }

    private fun customBridgeOrNull(): CustomBridgeInfo? {
        val feedProvider = settings.feedProvider.firstBlocking()
        return if (feedProvider.isNotBlank()) {
            val bridge = CustomBridgeInfo(feedProvider)
            if (bridge.isAvailable()) bridge else null
        } else {
            null
        }
    }

    private fun customBridgeAvailable() = customBridgeOrNull()?.isAvailable() == true

    fun isInstalled(): Boolean {
        return customBridgeAvailable() || !shouldUseFeed || bridgePackages.any { it.isAvailable() }
    }

    fun resolveSmartspace(): String {
        return bridgePackages.firstOrNull { it.supportsSmartspace }?.packageName
            ?: "com.google.android.googlequicksearchbox"
    }

    open inner class BridgeInfo(val packageName: String, signatureHashRes: Int) {
        protected open val signatureHash =
            if (signatureHashRes > 0) context.resources.getInteger(signatureHashRes) else 0

        open val supportsSmartspace = false

        fun isAvailable(): Boolean {
            val info = context.packageManager.resolveService(
                Intent(overlayAction)
                    .setPackage(packageName)
                    .setData(
                        Uri.parse(
                            StringBuilder(packageName.length + 18)
                                .append("app://")
                                .append(context.packageName)
                                .append(":")
                                .append(Process.myUid())
                                .toString()
                        )
                            .buildUpon()
                            .appendQueryParameter("v", 7.toString())
                            .appendQueryParameter("cv", 9.toString())
                            .build()
                    ), 0
            )
            return info != null //&& isSigned()
        }

        open fun isSigned(): Boolean {
            when {
                BuildConfig.DEBUG -> return true
                Utilities.ATLEAST_P -> {
                    val info =
                        context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    val signingInfo = info.signingInfo
                    if (signingInfo.hasMultipleSigners()) return false
                    return signingInfo.signingCertificateHistory.any { it.hashCode() == signatureHash }
                }
                else -> {
                    val info = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                    return if (info.signatures.any { it.hashCode() != signatureHash }) false else info.signatures.isNotEmpty()
                }
            }
        }
    }

    private inner class CustomBridgeInfo(packageName: String) : BridgeInfo(packageName, 0) {
        override val signatureHash = whitelist[packageName]?.toInt() ?: -1
        override fun isSigned(): Boolean {
            if (signatureHash == -1 && Utilities.ATLEAST_P) {
                val info = context.packageManager
                    .getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = info.signingInfo
                if (signingInfo.hasMultipleSigners()) return false
                signingInfo.signingCertificateHistory.forEach {
                    val hash = Integer.toHexString(it.hashCode())
                    Log.d(TAG, "Feed provider $packageName(0x$hash) isn't whitelisted")
                }
            }
            return signatureHash != -1 && super.isSigned()
        }
    }

    private inner class PixelBridgeInfo(packageName: String, signatureHashRes: Int) :
        BridgeInfo(packageName, signatureHashRes) {
        override val supportsSmartspace get() = isAvailable()
    }

    companion object {

        @JvmField
        val INSTANCE = MainThreadInitializedObject(::FeedBridge)

        private const val TAG = "FeedBridge"
        private const val overlayAction = "com.android.launcher3.WINDOW_OVERLAY"

        private val whitelist = mapOf<String, Long>(
            "ua.itaysonlab.homefeeder" to 0x887456ed, // HomeFeeder, t.me/homefeeder
            "launcher.libre.dev" to 0x2e9dbab5 // Librechair, t.me/librechair
        )

        fun getAvailableProviders(context: Context) = context.packageManager
            .queryIntentServices(
                Intent(overlayAction).setData(Uri.parse("app://${context.packageName}")),
                PackageManager.GET_META_DATA
            )
            .asSequence()
            .map { it.serviceInfo.applicationInfo }
            .distinct()
            .filter { INSTANCE.get(context).CustomBridgeInfo(it.packageName).isSigned() }

        @JvmStatic
        fun useBridge(context: Context) = INSTANCE.get(context).let { it.shouldUseFeed || it.customBridgeAvailable() }
    }
}