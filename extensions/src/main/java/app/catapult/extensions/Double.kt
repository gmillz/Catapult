package app.catapult.extensions

import kotlin.math.floor

fun Double.round() = floor(x = this).let {
    if (this - it >= 0.5) it + 1 else it
}