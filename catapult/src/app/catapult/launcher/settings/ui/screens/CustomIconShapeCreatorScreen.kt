package app.catapult.launcher.settings.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.catapult.launcher.icons.shape.IconCornerShape
import app.catapult.launcher.icons.shape.IconShape
import app.catapult.launcher.settings
import com.android.launcher3.R
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.components.AlertBottomSheetContent
import com.gmillz.compose.settings.ui.components.BottomSpacer
import com.gmillz.compose.settings.ui.components.SettingDivider
import com.gmillz.compose.settings.ui.components.SettingGroup
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.ui.components.bottomSheetHandler
import com.gmillz.compose.settings.ui.components.getSteps
import com.gmillz.compose.settings.ui.components.snapSliderValue
import com.gmillz.compose.settings.util.LocalNavController
import kotlin.math.roundToInt

@Composable
fun CustomIconShapeCreatorScreen() {

    val navController = LocalNavController.current
    val customIconShapeController = settings.customIconShape.getController()

    val appliedIconShape = customIconShapeController.state.value
    val selectedIconShape = remember { mutableStateOf(appliedIconShape?: IconShape.Circle) }
    val selectedIconShapeApplied = remember(appliedIconShape, selectedIconShape) {
        derivedStateOf {
            appliedIconShape.toString() == selectedIconShape.value.toString()
        }
    }

    SettingsPage(
        navController = navController,
        title = { Text(text = stringResource(id = R.string.custom_icon_shape)) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Button(
                    enabled = !selectedIconShapeApplied.value,
                    onClick = {
                        customIconShapeController.onChange(selectedIconShape.value)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Text(
                        text = if (appliedIconShape != null)
                            stringResource(id = R.string.apply)
                        else
                            stringResource(id = R.string.create)
                    )
                }
                BottomSpacer()
            }
        }
    ) {
        IconShapePreview(
            modifier = Modifier
                .padding(12.dp),
            iconShape = selectedIconShape.value
        )

        IconShapeCornerSettingGroup(
            selectedIconShape = selectedIconShape.value,
            onSelectedIconShapeChange = { newIconShape ->
                selectedIconShape.value = newIconShape
            }
        )
    }
}

@Composable
fun IconShapeCornerSettingGroup(
    selectedIconShape: IconShape,
    onSelectedIconShapeChange: (IconShape) -> Unit
) {
    SettingGroup(title = stringResource(id = R.string.sliders)) {
        IconShapeCornerSetting(
            title = stringResource(id = R.string.icon_shape_top_left),
            scale = selectedIconShape.topLeft.scale.x,
            onScaleChange = {
                onSelectedIconShapeChange(selectedIconShape.copy(topLeftScale = it))
            },
            cornerShape = selectedIconShape.topLeft.shape,
            onCornerShapeChange = {
                onSelectedIconShapeChange(selectedIconShape.copy(topLeftShape = it))
            }
        )
        
        IconShapeCornerSetting(
            title = stringResource(id = R.string.icon_shape_top_right),
            scale = selectedIconShape.topRight.scale.x,
            onScaleChange = {
                onSelectedIconShapeChange(selectedIconShape.copy(topRightScale = it))            
            },
            cornerShape = selectedIconShape.topRight.shape,
            onCornerShapeChange = {
                onSelectedIconShapeChange(selectedIconShape.copy(topRightShape = it))
            }
        )

        IconShapeCornerSetting(
            title = stringResource(id = R.string.icon_shape_bottom_left),
            scale = selectedIconShape.bottomLeft.scale.x,
            onScaleChange = {
                onSelectedIconShapeChange(selectedIconShape.copy(bottomLeftScale = it))
            },
            cornerShape = selectedIconShape.bottomLeft.shape,
            onCornerShapeChange = {
                onSelectedIconShapeChange(selectedIconShape.copy(bottomLeftShape = it))
            },
        )

        IconShapeCornerSetting(
            title = stringResource(id = R.string.icon_shape_bottom_right),
            scale = selectedIconShape.bottomRight.scale.x,
            onScaleChange = {
                onSelectedIconShapeChange(selectedIconShape.copy(bottomRightScale = it))
            },
            cornerShape = selectedIconShape.bottomRight.shape,
            onCornerShapeChange = {
                onSelectedIconShapeChange(selectedIconShape.copy(bottomRightShape = it))
            },
        )
    }
}

@Composable
fun IconShapeCornerSetting(
    modifier: Modifier = Modifier,
    title: String,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    cornerShape: IconCornerShape,
    onCornerShapeChange: (IconCornerShape) -> Unit
) {
    CornerSlider(
        modifier = modifier,
        label = title,
        value = scale,
        onValueChange = { newValue ->
            onScaleChange(newValue)
        },
        cornerShape = cornerShape,
        onCornerShapeChange = onCornerShapeChange
    )
}

@Composable
private fun CornerSlider(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    cornerShape: IconCornerShape,
    onCornerShapeChange: (IconCornerShape) -> Unit,
) {
    val bottomSheetHandler = bottomSheetHandler
    val options = listOf<IconCornerShape>(
        IconCornerShape.arc,
        IconCornerShape.Squircle,
        IconCornerShape.Cut,
    )

    val step = 0.1f
    val valueRange = 0f..1f

    SettingTemplate(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(text = label)
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onBackground,
                ) {
                    val valueText = stringResource(
                        id = R.string.n_percent,
                        (snapSliderValue(valueRange.start, value, step) * 100).roundToInt()
                    )
                    Text(text = valueText)
                }
            }
        },
        description = {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    steps = getSteps(valueRange, step),
                    modifier = Modifier
                        .height(24.dp)
                        .weight(1f)
                        .padding(horizontal = 3.dp),
                )
            }
        },
        endWidget = {
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        top = 12.dp,
                    )
                    .clip(shape = MaterialTheme.shapes.small)
                    .clickable {
                        bottomSheetHandler.show {
                            AlertBottomSheetContent(
                                title = { Text(stringResource(id = R.string.icon_shape_corner)) },
                                buttons = {
                                    OutlinedButton(onClick = { bottomSheetHandler.hide() }) {
                                        Text(text = stringResource(id = android.R.string.cancel))
                                    }
                                }
                            ) {
                                LazyColumn {
                                    itemsIndexed(options) { index, option ->
                                        if (index > 0) {
                                            SettingDivider(startIndent = 40.dp)
                                        }
                                        val selected = cornerShape::class.java == option::class.java
                                        SettingTemplate(
                                            title = {
                                                Text(
                                                    text = option.getLabel(),
                                                )
                                            },
                                            modifier = Modifier.clickable {
                                                bottomSheetHandler.hide()
                                                onCornerShapeChange(option)
                                            },
                                            startWidget = {
                                                RadioButton(
                                                    selected = selected,
                                                    onClick = null
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    .padding(
                        start = 8.dp,
                        top = 4.dp,
                        bottom = 4.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.requiredWidthIn(min = 48.dp),
                    text = cornerShape.getLabel(),
                    fontSize = 14.sp,
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                )
            }
        },
        applyPaddings = false,
    )
}

@Composable
private fun IconCornerShape.getLabel() = when (this) {
    IconCornerShape.Squircle -> stringResource(id = R.string.icon_shape_corner_squircle)
    IconCornerShape.Cut -> stringResource(id = R.string.icon_shape_corner_cut)
    else -> stringResource(id = R.string.icon_shape_corner_round)
}