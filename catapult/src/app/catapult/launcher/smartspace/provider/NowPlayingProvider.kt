package app.catapult.launcher.smartspace.provider

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Icon
import app.catapult.extensions.getAppName
import app.catapult.launcher.BlankActivity
import app.catapult.launcher.settings.Routes
import app.catapult.launcher.settings.SettingsActivity
import app.catapult.launcher.settings.ui.components.isNotificationServiceEnabled
import app.catapult.launcher.settings.ui.components.notificationDotsEnabled
import app.catapult.launcher.smartspace.model.SmartspaceAction
import app.catapult.launcher.smartspace.model.SmartspaceScores
import app.catapult.launcher.smartspace.model.SmartspaceTarget
import com.android.launcher3.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first

class NowPlayingProvider(context: Context) : SmartspaceDataSource(
    context, R.string.smartspace_now_playing, { smartspaceNowPlaying },
) {

    private val defaultIcon = Icon.createWithResource(context, R.drawable.ic_music_note)

    override val internalTargets = callbackFlow {
        val mediaListener = MediaListener(context) {
            trySend(listOfNotNull(getSmartspaceTarget(it)))
        }
        mediaListener.onResume()
        awaitClose { mediaListener.onPause() }
    }

    private fun getSmartspaceTarget(media: MediaListener): SmartspaceTarget? {
        val tracking = media.tracking ?: return null
        val title = tracking.info.title ?: return null

        val sbn = tracking.sbn
        val icon = sbn.notification.smallIcon ?: defaultIcon

        val mediaInfo = tracking.info
        val subtitle = mediaInfo.artist?.takeIf { it.isNotEmpty() }
            ?: sbn?.getAppName(context)
            ?: context.getAppName(tracking.packageName)
        val intent = sbn?.notification?.contentIntent
        return SmartspaceTarget(
            id = "nowPlaying-${mediaInfo.hashCode()}",
            headerAction = SmartspaceAction(
                id = "nowPlayingAction-${mediaInfo.hashCode()}",
                icon = icon,
                title = title,
                subtitle = subtitle,
                pendingIntent = intent,
                onClick = if (intent == null) Runnable { media.toggle(true) } else null,
            ),
            score = SmartspaceScores.SCORE_MEDIA,
            featureType = SmartspaceTarget.FeatureType.FEATURE_MEDIA,
        )
    }

    override suspend fun requiresSetup(): Boolean =
        isNotificationServiceEnabled(context = context).not() ||
            notificationDotsEnabled(context = context).first().not()

    override suspend fun startSetup(activity: Activity) {
        val intent = SettingsActivity.getIntent(activity, Routes.GENERAL)
        val message = activity.getString(R.string.event_provider_missing_notification_dots,
            activity.getString(providerName))
        BlankActivity.startBlankActivityDialog(
            activity,
            intent,
            activity.getString(R.string.title_missing_notification_access),
            message,
            context.getString(R.string.title_change_settings),
        )
    }
}
