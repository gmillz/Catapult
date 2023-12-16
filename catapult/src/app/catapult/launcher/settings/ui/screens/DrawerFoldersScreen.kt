package app.catapult.launcher.settings.ui.screens

import android.content.ComponentName
import android.util.Log
import android.widget.LinearLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import app.catapult.launcher.data.drawer.DrawerFolderRepository
import app.catapult.launcher.launcher
import app.catapult.launcher.model.DrawerFolder
import app.catapult.launcher.settings.ui.components.SelectAppsBottomSheet
import com.android.launcher3.R
import com.android.launcher3.folder.FolderIcon
import com.gmillz.compose.settings.ui.components.AlertBottomSheetContent
import com.gmillz.compose.settings.ui.components.BottomSheetHandler
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.ui.components.bottomSheetHandler
import com.gmillz.compose.settings.util.LocalNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerFoldersScreen() {
    val context = LocalContext.current
    val scope = CoroutineScope(Job() + Dispatchers.IO)
    val folderBottomSheetState = rememberModalBottomSheetState()

    SettingsPage(
        title = { Text(text = stringResource(R.string.drawer_folders)) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val folder = DrawerFolder(id = 0, title = "New Folder", listOf())
                    scope.launch {
                        val folderId = DrawerFolderRepository.INSTANCE.get(context).putFolder(folder)
                        // TODO open new item
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) {
        DrawerFolders(folderBottomSheetState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerFolderItem(
    folder: DrawerFolder,
    bottomSheetState: SheetState,
    updateFolder: (DrawerFolder?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showEditSheet by remember { mutableStateOf(false) }


    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = bottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            DrawerFolderEditorBottomSheet(folder) {
                showEditSheet = false
                updateFolder(it)
            }
        }
    }


    SettingTemplate(
        title = folder.title,
        description = "${folder.content.size} apps",
        startWidget = {
            AndroidView(
                factory = {
                    val icon = FolderIcon.inflateIcon(R.layout.folder_icon, launcher, LinearLayout(launcher, null), folder.asFolderInfo())
                    icon.textVisible = false
                    icon
                },
                update = {
                    it.mInfo = folder.asFolderInfo()
                    it.refreshDrawableState()
                    it.postInvalidate()
                    it.onItemsChanged(true)
                },
                modifier = Modifier.size(60.dp))
        },
        endWidget = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.clickable {
                    scope.launch {
                        DrawerFolderRepository.INSTANCE.get(context).deleteFolder(folder)
                    }
                }
            )
        },
        onClick = {
            scope.launch {
                showEditSheet = true
                bottomSheetState.show()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerFolderEditorBottomSheet(
    folder: DrawerFolder,
    onDismissRequest: (DrawerFolder?) -> Unit
) {
    val context = LocalContext.current
    val selectAppsBottomSheetState = rememberModalBottomSheetState()
    var showAppsBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val selectedApps = remember { mutableStateListOf<ComponentName>() }
    LaunchedEffect(Unit) {
        selectedApps.addAll(folder.content)
    }

    if (showAppsBottomSheet) {
        SelectAppsBottomSheet(
            title = "Select Apps",
            sheetState = selectAppsBottomSheetState,
            currentSelectedApps = selectedApps,
            onDismissRequest = {
                Log.d("TEST", "SelectApps onDismissRequest")
                showAppsBottomSheet = false
                scope.launch {
                    selectAppsBottomSheetState.hide()
                }
            }
        ) {
            selectedApps.clear()
            selectedApps.addAll(it.distinct())
        }
    }

    var title by remember { mutableStateOf(folder.title) }
    AlertBottomSheetContent(
        title = {
            Row {
                /*AndroidView(
                    factory = {
                        val icon = FolderIcon.inflateIcon(
                            R.layout.folder_icon,
                            launcher,
                            LinearLayout(launcher, null),
                            folder.asFolderInfo()
                        )
                        icon.textVisible = false
                        icon
                    },
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.CenterVertically)
                )*/
                OutlinedTextField(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp)
                        .fillMaxWidth(),
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = "Title") }
                )
            }
        },
        text = {
               Column {
                   ListItem(
                       modifier = Modifier
                           .height(80.dp)
                           .clickable {
                               scope.launch {
                                   showAppsBottomSheet = true
                                   selectAppsBottomSheetState.show()
                               }
                           },
                       headlineContent = {
                           Text(
                               text = "Select apps",
                               style = MaterialTheme.typography.headlineMedium
                           )
                       },
                       supportingContent = {
                           Text(
                               text = selectedApps.size.toString() + " Apps"
                           )
                       },
                       leadingContent = {
                           Icon(imageVector = Icons.Default.Apps, contentDescription = null)
                       },
                       trailingContent = {
                           Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null)
                       }
                   )
               }
        },
        buttons = {
            OutlinedButton(
                onClick = { onDismissRequest(null) },
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            Spacer(modifier = Modifier.requiredWidth(8.dp))
            Button(
                onClick = {
                    val newFolder = DrawerFolder(
                        id = folder.id,
                        title = title,
                        content = selectedApps.distinct()
                    )
                    scope.launch {
                        DrawerFolderRepository.INSTANCE.get(context)
                            .putFolder(newFolder)
                    }
                    onDismissRequest(newFolder)
                }
            ) {
                Text(text = stringResource(id = com.google.android.material.R.string.mtrl_picker_save))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerFolders(bottomSheetState: SheetState) {

    val folders = remember {
        mutableStateListOf<DrawerFolder>()
    }
    
    val foldersState by DrawerFolderRepository.INSTANCE.get(LocalContext.current)
        .getFolders().collectAsState(initial = emptyList())
    
    LaunchedEffect(foldersState) {
        folders.clear()
        folders.addAll(foldersState)
    }

    for (folder in folders) {
            DrawerFolderItem(folder, bottomSheetState) { drawerFolder ->
                if (drawerFolder == null) return@DrawerFolderItem
                val index = folders.indexOfFirst { it.id == folder.id }
                folders.removeAt(index)
                folders.add(index, drawerFolder)
            }
    }
}
