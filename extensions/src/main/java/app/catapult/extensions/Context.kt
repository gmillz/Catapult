package app.catapult.extensions

import android.content.Context

import androidx.core.content.getSystemService

fun Context.isThemedIconsEnabled() = true
inline fun <reified T : Any> Context.requireSystemService(): T = checkNotNull(getSystemService())
