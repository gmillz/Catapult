package app.catapult.launcher.settings.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import app.catapult.launcher.util.App
import com.gmillz.compose.settings.ui.components.SettingTemplate

@Composable
fun AppItem(
    app: App,
    onClick: (app: App) -> Unit,
    widget: (@Composable () -> Unit)? = null
) {
    AppItem(
        label = app.label,
        icon = app.icon,
        onClick = { onClick(app) },
        widget = widget
    )
}

@Composable
fun AppItem(
    label: String,
    icon: Bitmap,
    onClick: () -> Unit,
    widget: (@Composable () -> Unit)? = null
) {
    SettingTemplate(
        title = label,
        startWidget = {
            widget?.let {
                it()
                Spacer(modifier = Modifier.requiredWidth(12.dp))
            }
            Icon(
                bitmap = icon.asImageBitmap(),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(34.dp)
            )
        },
        onClick = onClick
    )
}
