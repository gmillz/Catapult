package app.catapult.launcher.popup

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.catapult.launcher.data.overrides.ItemOverride
import app.catapult.launcher.data.overrides.ItemOverrideRepository
import app.catapult.launcher.icons.IconEntry
import app.catapult.launcher.icons.IconPackProvider
import app.catapult.launcher.icons.IconPickerItem
import app.catapult.launcher.settings.SettingsActivity
import app.catapult.launcher.settings.ui.components.ClickableIcon
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.gmillz.compose.settings.extensions.addIfNotNull
import com.gmillz.compose.settings.extensions.navigationBarsOrDisplayCutoutPadding
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeDialog(
    icon: Drawable,
    title: String,
    onTitleChange: (String) -> Unit,
    defaultTitle: String,
    launchSelectIcon: (() -> Unit)?,
    content: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .navigationBarsOrDisplayCutoutPadding()
            .fillMaxWidth()
    ) {
        val iconPainter = rememberDrawablePainter(drawable = icon)
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .addIfNotNull(launchSelectIcon) {
                    clickable(onClick = it)
                }
                .padding(all = 8.dp)
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                tint = Color.Unspecified
            )
        }
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            trailingIcon = {
                if (title != defaultTitle) {
                    ClickableIcon(
                        painter = painterResource(id = R.drawable.ic_undo),
                        onClick = { onTitleChange(defaultTitle) })
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12F),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(24.dp),
            label = { Text(text = stringResource(id = R.string.label)) },
            isError = title.isEmpty(),
        )
        content?.invoke()
    }
}

@Composable
fun CustomizeAppDialog(
    icon: Drawable,
    defaultTitle: String,
    componentKey: ComponentKey,
    container: Int,
    onClose: () -> Unit
) {
    Log.d("TEST", "CustomizeAppDialog, $container, $componentKey")
    val context = LocalContext.current
    val itemRepo = ItemOverrideRepository.INSTANCE.get(LocalContext.current)
    val itemOverride = remember {
        mutableStateOf(ItemOverride(0, componentKey, null, null, container))
    }
    val iconEntry = remember { mutableStateOf<IconEntry?>(null) }
    val iconDrawable =
        if (iconEntry.value != null) {
            IconPackProvider.INSTANCE.get(context).getDrawable(iconEntry.value!!, 56.dp.value.toInt(), componentKey.user)!!
        } else {
            icon
        }

    var title by remember { mutableStateOf(defaultTitle) }
    var itemSet by remember { mutableStateOf(false) }

    val request = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            Log.d("TEST", "onResult")
            if (it.resultCode != Activity.RESULT_OK) {
                return@rememberLauncherForActivityResult
            }
            if (it.data?.hasExtra("icon_picker_item") == true) {
                val item: IconPickerItem =
                    it.data?.getParcelableExtra("icon_picker_item")!!
                iconEntry.value = item.toIconEntry()
                CoroutineScope(Dispatchers.IO).launch {
                    itemOverride.value.iconPickerItem = item
                    Log.d("TEST", "set new icon")
                }
            }
            //onClose()
        }
    )

    val openIconPicker = {
        request.launch(SettingsActivity.createSelectIconIntent(context, componentKey))
    }

    if (!itemSet) {
        SideEffect {
            CoroutineScope(Dispatchers.IO).launch {
                val item = itemRepo.get(componentKey, container)
                MainScope().launch {
                    Log.d("TEST", "setting itemOverride for $componentKey")
                    if (item != null) {
                        itemOverride.value = item
                        if (!item.overrideTitle.isNullOrEmpty()) {
                            title = item.overrideTitle!!
                        }
                        itemSet = true
                    }
                }
            }
        }
    }

    DisposableEffect(key1 = itemOverride) {
        //title = itemOverride.value.overrideTitle?: defaultTitle
        onDispose {
            Log.d("TEST", "onDispose")
            val previousTitle = itemOverride.value.overrideTitle
            val newTitle = if (title != defaultTitle) title else null
            if (newTitle != previousTitle) {
                itemOverride.value.overrideTitle = title
            }
            CoroutineScope(Dispatchers.IO).launch {
                itemRepo.put(itemOverride.value)
                MainScope().launch {
                    LauncherAppState.getInstance(context).model
                        .onPackageChanged(componentKey.componentName.packageName, componentKey.user)
                }
            }
        }
    }

    CustomizeDialog(
        icon = iconDrawable,
        title = title,
        onTitleChange = { title = it },
        defaultTitle = defaultTitle,
        launchSelectIcon = openIconPicker)
}
