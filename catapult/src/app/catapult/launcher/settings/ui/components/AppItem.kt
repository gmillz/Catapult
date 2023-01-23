package app.catapult.launcher.settings.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.ui.components.SettingTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppItem(
    label: String,
    icon: Bitmap,
    onClick: () -> Unit
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
        onClick = onClick
    )
}
