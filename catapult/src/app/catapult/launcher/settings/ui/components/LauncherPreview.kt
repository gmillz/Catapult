package app.catapult.launcher.settings.ui.components

import android.view.View
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import app.catapult.launcher.LauncherPreviewManager
import app.catapult.launcher.settings.ui.theme.SettingsTheme
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherAppState
import com.gmillz.compose.settings.util.lifecycleState

@Composable
fun DummyLauncherBox(
    modifier: Modifier = Modifier,
    darkText: Boolean = wallpaperSupportsDarkText(),
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val idp = remember { InvariantDeviceProfile.INSTANCE.get(context) }
    val dp = idp.getDeviceProfile(context)
    val ratio = dp.widthPx.toFloat() / dp.heightPx.toFloat()

    Box(modifier = modifier.aspectRatio(ratio, matchHeightConstraintsFirst = true)) {
        SettingsTheme(darkTheme = !darkText) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            )
        }
        content()
    }
}

@Composable
fun DummyLauncherLayout(
    idp: InvariantDeviceProfile,
    modifier: Modifier = Modifier,
) {
    val previewView = createPreviewView(idp)
    Crossfade(targetState = previewView, label = "") { view ->
        AndroidView(
            factory = { context ->
                view ?: View(context)
            },
            modifier = modifier
        )
    }
}

@Composable
fun wallpaperSupportsDarkText(): Boolean {
    //val context = LocalContext.current
    //val colorHints = remember {
    //    WallpaperManagerCompat.INSTANCE.get(context).systemColors?.colorHints ?: 0
    //}
    //return (colorHints and WallpaperColorsCompat.HINT_SUPPORTS_DARK_TEXT) != 0
    return true
}

@Composable
fun invariantDeviceProfile(): InvariantDeviceProfile {
    return LauncherAppState.getIDP(LocalContext.current)

}

@Composable
fun createPreviewView(
    idp: InvariantDeviceProfile = invariantDeviceProfile()
): View? {
    val context = LocalContext.current
    val lifecycleState = lifecycleState()
    if (!lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
        return null
    }
    val previewManager = remember { LauncherPreviewManager(context) }
    return remember(idp) { previewManager.createPreviewView(idp) }
}
