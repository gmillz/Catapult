package app.catapult.launcher.settings.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun ClickableIcon(
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        val contentAlpha = if (enabled) tint.alpha else .37f
        val alpha by animateFloatAsState(targetValue = contentAlpha)
        Icon(
            painter = painter,
            contentDescription = null,
            tint = tint.copy(alpha = alpha)
        )
    }
}
