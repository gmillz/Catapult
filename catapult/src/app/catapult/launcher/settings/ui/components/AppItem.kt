package app.catapult.launcher.settings.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.ui.components.SettingTemplate

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
            Icon(
                bitmap = icon.asImageBitmap(),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(34.dp)
            )
        },
        endWidget = {
            widget?.invoke()
        },
        onClick = onClick
    )
}

@Composable
fun AppItemPlaceholder(
    widget: (@Composable () -> Unit)? = null
) {
    SettingTemplate(
        title = {
            Spacer(modifier = Modifier
                .width(120.dp)
                .height(24.dp)
                .placeholder(
                    visible = true,
                    highlight = PlaceholderHighlight.fade()
                ))
        },
        startWidget = {
            Spacer(
                modifier = Modifier
                    .size(30.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.fade()
                    )
            )
        }
    )
}
