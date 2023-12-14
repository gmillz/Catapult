package app.catapult.launcher.settings.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.catapult.launcher.DeviceProfileOverrides
import com.android.launcher3.InvariantDeviceProfile

@Composable
fun GridPreview(
    modifier: Modifier = Modifier,
    updateGridOptions: DeviceProfileOverrides.DbGridInfo.() -> DeviceProfileOverrides.DbGridInfo
) {
    DummyLauncherBox(modifier = modifier) {
        WallpaperPreview(modifier = Modifier.fillMaxSize())
        DummyLauncherLayout(
            idp = createPreviewIdp(updateGridOptions),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun createPreviewIdp(updateGridOptions: DeviceProfileOverrides.DbGridInfo.() -> DeviceProfileOverrides.DbGridInfo): InvariantDeviceProfile {
    val context = LocalContext.current

    val newIdp by remember {
        derivedStateOf {
            val options = DeviceProfileOverrides.DbGridInfo()
            InvariantDeviceProfile(context, updateGridOptions(options))
        }
    }
    return newIdp
}