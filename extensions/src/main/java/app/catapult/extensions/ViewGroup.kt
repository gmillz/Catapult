package app.catapult.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

val ViewGroup.recursiveChildren: Sequence<View>
    get() = children.flatMap {
        if (it is ViewGroup) {
            it.recursiveChildren + sequenceOf(it)
        } else sequenceOf(it)
    }