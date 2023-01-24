package app.catapult.extensions

fun Int.hasFlag(flag: Int): Boolean {
    return (this and flag) == flag
}