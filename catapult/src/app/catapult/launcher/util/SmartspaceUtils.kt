package app.catapult.launcher.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.res.Resources
import android.view.View

@SuppressLint("DiscouragedApi")
private val pendingIntentTagId =
    Resources.getSystem().getIdentifier("pending_intent_tag", "id", "android")

val View?.pendingIntent get() = this?.getTag(pendingIntentTagId) as? PendingIntent