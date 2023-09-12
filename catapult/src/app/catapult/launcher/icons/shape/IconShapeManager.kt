package app.catapult.launcher.icons.shape

import android.content.Context
import android.graphics.Path
import android.graphics.Region
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import app.catapult.launcher.settings
import com.android.launcher3.Utilities
import com.android.launcher3.icons.GraphicsUtils
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.util.MainThreadInitializedObject

class IconShapeManager(private val context: Context) {

    private val systemIconShape = getSystemShape()

    private fun getSystemShape(): IconShape {

        val iconMask = AdaptiveIconDrawable(null, null).iconMask
        val systemShape = findNearestShape(iconMask)
        return object : IconShape(systemShape) {

            override fun getMaskPath(): Path {
                return Path(iconMask)
            }

            override fun toString() = "system"

            override fun getHashString(): String {
                val resId = IconProvider.CONFIG_ICON_MASK_RES_ID
                if (resId == 0) {
                    return "system-path"
                }
                return context.getString(resId)
            }
        }
    }

    private fun findNearestShape(comparePath: Path): IconShape {
        val size = 200
        val clip = Region(0, 0, size, size)
        val iconR = Region().apply {
            setPath(comparePath, clip)
        }
        val shapePath = Path()
        val shapeR = Region()
        return listOf(
            IconShape.Circle,
            IconShape.Square,
            IconShape.RoundedSquare,
            IconShape.Squircle,
            IconShape.Sammy,
            IconShape.Teardrop,
            IconShape.Cylinder
        )
            .minByOrNull {
                shapePath.reset()
                it.addShape(shapePath, 0f, 0f, size / 2f)
                shapeR.setPath(shapePath, clip)
                shapeR.op(iconR, Region.Op.XOR)

                GraphicsUtils.getArea(shapeR)
            }!!
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::IconShapeManager)

        fun getSystemIconShape(context: Context) = INSTANCE.get(context).systemIconShape

        @JvmStatic
        fun getWindowTransitionRadius(context: Context) = settings.iconShape.firstBlocking().windowTransitionRadius
    }
}