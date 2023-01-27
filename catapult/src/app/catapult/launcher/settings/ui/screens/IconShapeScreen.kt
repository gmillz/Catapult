package app.catapult.launcher.settings.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.catapult.launcher.icons.shape.IconShape
import app.catapult.launcher.icons.shape.IconShapeManager
import app.catapult.launcher.settings
import app.catapult.launcher.settings.Routes
import com.android.launcher3.R
import com.gmillz.compose.settings.SettingController
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.util.LocalNavController

fun iconShapeEntries(context: Context): Map<IconShape, Int> = mapOf(
    IconShapeManager.getSystemIconShape(context) to R.string.icon_shape_system,
    IconShape.Circle to R.string.icon_shape_circle,
    IconShape.Cylinder to R.string.icon_shape_cylinder,
    IconShape.Diamond to R.string.icon_shape_diamond,
    IconShape.Egg to R.string.icon_shape_egg,
    IconShape.Cupertino to R.string.icon_shape_cupertino,
    IconShape.Octagon to R.string.icon_shape_octagon,
    IconShape.Sammy to R.string.icon_shape_sammy,
    IconShape.RoundedSquare to R.string.icon_shape_rounded_square,
    IconShape.SharpSquare to R.string.icon_shape_sharp_square,
    IconShape.Square to R.string.icon_shape_square,
    IconShape.Squircle to R.string.icon_shape_squircle,
    IconShape.Teardrop to R.string.icon_shape_teardrop
)

@Composable
fun IconShapeScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val entries = remember {
        iconShapeEntries(context)
    }
    val iconShapeController = settings.iconShape.getController()
    val customIconShape = settings.customIconShape.getController().state

    SettingsPage(
        navController = navController,
        title = { Text(text = stringResource(id = R.string.icon_shape_title)) }
    ) {
        SettingGroup(
            title = stringResource(id = R.string.custom)
        ) {
            CustomIconShapeSetting(
                iconShapeController = iconShapeController
            )
            ModifyCustomIconShapeSetting(
                customIconShape = customIconShape.value
            )
        }
        SettingGroup(
            title = stringResource(id = R.string.presets)
        ) {
            entries.forEach { (shape, stringResourceId) ->
                SettingTemplate(
                    enabled = true,
                    title = { Text(text = stringResource(id = stringResourceId)) },
                    modifier = Modifier.clickable {
                        iconShapeController.onChange(newValue = shape)
                    },
                    startWidget = {
                        RadioButton(
                            selected = shape == iconShapeController.state.value,
                            onClick = null
                        )
                    },
                    endWidget = {
                        IconShapePreview(iconShape = shape)
                    }
                )
            }
        }
    }
}

@Composable
fun CustomIconShapeSetting(
    iconShapeController: SettingController<IconShape>,
) {
    val customIconShapeController = settings.customIconShape.getController()
    val customIconShape = customIconShapeController.state.value

    customIconShape?.let {
        SettingTemplate(
            title = { Text(stringResource(id = R.string.custom)) },
            onClick = {
                iconShapeController.onChange(it)
            },
            startWidget = {
                RadioButton(
                    selected = IconShape.isCustomShape(iconShapeController.state.value),
                    onClick = null
                )
            },
            endWidget = {
                IconShapePreview(iconShape = it)
            }
        )
    }
}

@Composable
fun ModifyCustomIconShapeSetting(
    customIconShape: IconShape?
) {
    val navController = LocalNavController.current
    val created = customIconShape != null

    val text = if (created)
        stringResource(id = R.string.custom_icon_shape_edit)
    else
        stringResource(id = R.string.custom_icon_shape_create)

    val icon = if (created)
        Icons.Rounded.Edit
    else
        Icons.Rounded.Add

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Routes.CUSTOM_ICON_SHAPE_CREATOR)
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.secondary,
                LocalTextStyle provides MaterialTheme.typography.bodyMedium
            ) {
                Text(
                    text = text
                )
            }
            Spacer(modifier = Modifier.requiredWidth(12.dp))
            Icon(
                imageVector = icon,
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = null
            )
        }
    }
}

@Composable
fun IconShapePreview(
    modifier: Modifier = Modifier,
    iconShape: IconShape,
    strokeColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
) {

    val path = iconShape.getMaskPath().asComposePath()

    var translated = remember { false }
    fun translatePath(canvasWidth: Float, canvasHeight: Float) {
        if (!translated) {
            translated = true
            val pathHeight = path.getBounds().size.height
            val pathWidth = path.getBounds().size.width
            path.translate(
                Offset(
                    x = (canvasWidth - pathWidth) / 2,
                    y = (canvasHeight - pathHeight) / 2,
                ),
            )
        }
    }

    Canvas(
        modifier = modifier.requiredSize(48.dp),
    ) {
        translatePath(
            canvasWidth = size.width,
            canvasHeight = size.height,
        )
        drawPath(
            path = path,
            color = fillColor,
        )
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 4f),
        )
    }
}