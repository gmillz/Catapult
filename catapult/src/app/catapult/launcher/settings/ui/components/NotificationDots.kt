package app.catapult.launcher.settings.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.android.launcher3.R
import com.android.launcher3.notification.NotificationListener
import com.android.launcher3.settings.SettingsActivity.EXTRA_FRAGMENT_ARG_KEY
import com.android.launcher3.settings.SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGS
import com.android.launcher3.util.SettingsCache
import com.android.launcher3.util.SettingsCache.NOTIFICATION_BADGING_URI
import com.gmillz.compose.settings.ui.components.AlertBottomSheetContent
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.bottomSheetHandler
import com.gmillz.compose.settings.util.lifecycleState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@Composable
fun NotificationDotsSetting(enabled: Boolean, serviceEnabled: Boolean) {
    val bottomSheetHandler = bottomSheetHandler
    val context = LocalContext.current
    val showWarning = enabled && !serviceEnabled
    val summary = when {
        showWarning -> R.string.missing_notification_access_description
        enabled -> R.string.notification_dots_desc_on
        else -> R.string.notification_dots_desc_off
    }

    SettingTemplate(
        title = { Text(text = stringResource(id = R.string.notification_dots_title)) },
        description = { Text(text = stringResource(id = summary)) },
        endWidget = {
            if (showWarning) {
                Icon(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp),
                    painter = painterResource(id = R.drawable.ic_warning),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .68f)
                )
            }
        },
        onClick = {
            if (showWarning) {
                bottomSheetHandler.show {
                    NotificationAccessConfirmation {
                        bottomSheetHandler.hide()
                    }
                }
            } else {
                val extras = Bundle().apply {
                    putString(EXTRA_FRAGMENT_ARG_KEY, "notification_badging")
                }
                val intent = Intent("android.settings.NOTIFICATION_SETTINGS")
                    .putExtra(EXTRA_SHOW_FRAGMENT_ARGS, extras)
                context.startActivity(intent)
            }
        }
    )
}

@Composable
fun NotificationAccessConfirmation(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    AlertBottomSheetContent(
        title = { Text(text = stringResource(id = R.string.missing_notification_access_label)) },
        text = {
            val appName = stringResource(id = R.string.derived_app_name)
            Text(text = stringResource(id = R.string.msg_missing_notification_access, appName))
        },
        buttons = {
            OutlinedButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            Spacer(modifier = Modifier.requiredWidth(8.dp))
            Button(
                onClick = {
                    onDismissRequest()

                    val cn = ComponentName(context, NotificationListener::class.java)
                    val showFragmentArgs = Bundle()
                    showFragmentArgs.putString(EXTRA_FRAGMENT_ARG_KEY, cn.flattenToString())

                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(EXTRA_FRAGMENT_ARG_KEY, cn.flattenToString())
                        .putExtra(EXTRA_SHOW_FRAGMENT_ARGS, showFragmentArgs)
                    context.startActivity(intent)
                }
            ) {
                Text(text = stringResource(id = R.string.title_change_settings))
            }
        }
    ) {

    }
}

fun notificationDotsEnabled(context: Context) = callbackFlow {
    val observer = SettingsCache.OnChangeListener {
        val enabled = SettingsCache.INSTANCE.get(context).getValue(NOTIFICATION_BADGING_URI)
        trySend(enabled)
    }
    val settingsCache = SettingsCache.INSTANCE.get(context)
    observer.onSettingsChanged(false)
    settingsCache.register(NOTIFICATION_BADGING_URI, observer)
    awaitClose { settingsCache.unregister(NOTIFICATION_BADGING_URI, observer) }
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver, "enabled_notification_listeners")?: ""
    val myListener = ComponentName(context, NotificationListener::class.java)
    return (enabledListeners.contains(myListener.flattenToString())) ||
        enabledListeners.contains(myListener.flattenToShortString())
}

@Composable
fun notificationServiceEnabled(): Boolean {
    val context = LocalContext.current

    val enabledState = remember { mutableStateOf(isNotificationServiceEnabled(context)) }
    val resumed = lifecycleState().isAtLeast(Lifecycle.State.RESUMED)

    if (resumed) {
        DisposableEffect(null) {
            enabledState.value = isNotificationServiceEnabled(context)
            onDispose {  }
        }
    }
    return enabledState.value
}
