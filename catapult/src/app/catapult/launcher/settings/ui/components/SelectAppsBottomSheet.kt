package app.catapult.launcher.settings.ui.components

import android.annotation.SuppressLint
import android.content.ComponentName
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.ui.components.SettingsToolbar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAppsBottomSheet(
    title: String,
    sheetState: SheetState,
    currentSelectedApps: List<ComponentName>,
    onDismissRequest: () -> Unit,
    onSave: (List<ComponentName>) -> Unit
) {
    val selectedApps = remember { mutableStateListOf<ComponentName>() }
    selectedApps.addAll(currentSelectedApps)
    Log.d("TEST", "here")
    val apps by appsList()
    val state = rememberLazyListState()
    Log.d("TEST", "here1")

    ModalBottomSheet(
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        content = {
            Scaffold(
                topBar = {
                    SettingsToolbar(
                        title = { Text(text = title) },
                        onBack = { onDismissRequest() },
                        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                    )
                }
            ) {
                DisposableEffect(key1 = LocalLifecycleOwner.current) {
                    onDispose {
                        onSave(selectedApps)
                    }
                }
                Crossfade(targetState = apps.isNotEmpty(), label = "") { present ->
                    if (present) {
                        SettingsLazyColumn(state = state) {
                            settingGroupItems(apps) {app ->
                                AppItem(
                                    label = app.label,
                                    icon = app.icon,
                                    onClick = {
                                              if (selectedApps.contains(app.info.componentName)) {
                                                  selectedApps.remove(app.info.componentName)
                                              } else {
                                                  selectedApps.add(app.info.componentName)
                                              }
                                    },
                                    widget = {
                                        Checkbox(
                                            checked = selectedApps.contains(app.info.componentName),
                                            onCheckedChange = {
                                                if (it) {
                                                    selectedApps.add(app.info.componentName)
                                                } else {
                                                    selectedApps.remove(app.info.componentName)
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    } else {
                        SettingsLazyColumn(enabled = false) {
                            settingGroupItems(
                                count = 20,
                            ) {
                                AppItemPlaceholder {
                                    Spacer(Modifier.width(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    )
    Log.d("TEST", "here2")
}