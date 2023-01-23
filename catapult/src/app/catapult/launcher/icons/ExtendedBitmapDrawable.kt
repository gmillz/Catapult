package app.catapult.launcher.icons

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

class ExtendedBitmapDrawable(
    res: Resources,
    bitmap: Bitmap,
    val isFromIconPack: Boolean
): BitmapDrawable(res, bitmap) {
    companion object {
        fun wrap(res: Resources, drawable: Drawable?, isFromIconPack: Boolean): Drawable? {
            return if (drawable is BitmapDrawable) {
                ExtendedBitmapDrawable(res, drawable.bitmap, isFromIconPack)
            } else {
                drawable
            }
        }

        val Drawable.isFromIconPack get() = (this as? ExtendedBitmapDrawable)?.isFromIconPack?: false
    }
}
