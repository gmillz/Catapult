package app.catapult.launcher.settings.ui.components


import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WallpaperPreview(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wallpaperManager = remember { WallpaperManager.getInstance(context) }
    val wallpaperInfo = wallpaperManager.wallpaperInfo
    val permissionState =
            rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
    var wallpaperDrawable by remember {
        mutableStateOf<Drawable?>(null)
    }
    SideEffect {
        if (wallpaperDrawable == null) {
            when {
                wallpaperInfo != null -> wallpaperDrawable =
                    wallpaperInfo.loadThumbnail(context.packageManager)

                permissionState.status.isGranted -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        wallpaperManager.drawable?.let {
                            val size =
                                Size(
                                    it.intrinsicWidth,
                                    it.intrinsicHeight
                                ).scaleDownToDisplaySize(
                                    context
                                )
                            val bitmap = it.toBitmap(size.width, size.height)
                            wallpaperDrawable = BitmapDrawable(context.resources, bitmap)
                        }
                    }
                }
            }
        }
    }
    key(wallpaperDrawable, permissionState) {
        Image(
            painter = rememberDrawablePainter(drawable = wallpaperDrawable),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.FillHeight
        )
    }

    if (!permissionState.status.isGranted) {
        SideEffect {
            permissionState.launchPermissionRequest()
        }
    }
}

private fun Size.scaleDownToDisplaySize(context: Context): Size {
    val width = width
    val height = height

    val metrics = context.resources.displayMetrics
    val maxSize = max(metrics.widthPixels, metrics.heightPixels)

    return when {
        width > height && width > maxSize -> {
            val newHeight = (height * maxSize.toFloat() / width).toInt()
            Size(maxSize, newHeight)
        }

        height > maxSize -> {
            val newWidth = (width * maxSize.toFloat() / height).toInt()
            Size(newWidth, maxSize)
        }

        else -> this
    }

}