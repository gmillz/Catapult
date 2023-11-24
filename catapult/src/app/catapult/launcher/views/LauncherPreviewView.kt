package app.catapult.launcher.views

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_HOTSEAT
import com.android.launcher3.LauncherSettings.Favorites.SCREEN
import com.android.launcher3.LauncherSettings.Favorites.TABLE_NAME
import com.android.launcher3.R
import com.android.launcher3.graphics.LauncherPreviewRenderer
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.GridSizeMigrationUtil
import com.android.launcher3.model.LauncherBinder
import com.android.launcher3.model.LoaderTask
import com.android.launcher3.provider.LauncherDbUtils
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.android.launcher3.util.Executors.MODEL_EXECUTOR
import com.android.launcher3.util.RunnableList
import com.android.launcher3.util.Themes
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.lang.Float.min

class LauncherPreviewView(
    context: Context,
    private val idp: InvariantDeviceProfile,
    private val dummyInsets: Boolean = false,
    private val appContext: Context = context.applicationContext
) : FrameLayout(context) {

    private val onReadyCallbacks = RunnableList()
    private val onDestroyCallbacks = RunnableList()
    private var destroyed = false

    private var rendererView: View? = null

    private val spinner = CircularProgressIndicator(context).apply {
        val themedContext = ContextThemeWrapper(context, Themes.getActivityThemeRes(context))
        val textColor = Themes.getAttrColor(themedContext, R.attr.workspaceTextColor)
        isIndeterminate = true
        setIndicatorColor(textColor)
        trackCornerRadius = 1000
        alpha = 0f
        animate()
            .alpha(1f)
            .withLayer()
            .setStartDelay(100)
            .setDuration(300)
            .start()
    }

    init {
        addView(
            spinner,
            LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { gravity = Gravity.CENTER })
        loadAsync()
    }

    fun addOnReadyCallback(runnable: Runnable) {
        onReadyCallbacks.add(runnable)
    }

    @UiThread
    fun destroy() {
        destroyed = true
        onDestroyCallbacks.executeAllAndDestroy()
        removeAllViews()
    }

    private fun loadAsync() {
        MODEL_EXECUTOR.execute(this::loadModelData)
    }

    @WorkerThread
    private fun loadModelData() {
        val inflationContext = ContextThemeWrapper(appContext, Themes.getActivityThemeRes(context))
        if (GridSizeMigrationUtil.needsToMigrate(inflationContext, idp)) {
            val previewContext = LauncherPreviewRenderer.PreviewContext(inflationContext, idp)
            // Copy existing data to preview DB
            LauncherDbUtils.copyTable(LauncherAppState.getInstance(context)
                .model.modelDbController.db,
                TABLE_NAME,
                LauncherAppState.getInstance(previewContext)
                    .model.modelDbController.db,
                TABLE_NAME,
                context
            )
            LauncherAppState.getInstance(previewContext)
                .model.modelDbController.clearEmptyDbFlag()

            val bgModel = BgDataModel()
            object : LoaderTask(
                LauncherAppState.getInstance(previewContext),
                null,
                bgModel,
                LauncherAppState.getInstance(previewContext).model.modelDelegate,
                LauncherBinder(LauncherAppState.getInstance(previewContext), bgModel, null, arrayOf())
            ) {
                override fun run() {
                    loadWorkspace(
                        emptyList(),
                        "$SCREEN = 0 or $CONTAINER = $CONTAINER_HOTSEAT",
                        null
                    )
                    MAIN_EXECUTOR.execute {
                        renderView(previewContext, mBgDataModel, mWidgetProvidersMap)
                        onDestroyCallbacks.add { previewContext.onDestroy() }
                    }
                }
            }.run()
        } else {
            LauncherAppState.getInstance(inflationContext).model.loadAsync { dataModel ->
                if (dataModel != null) {
                    MAIN_EXECUTOR.execute {
                        renderView(inflationContext, dataModel, null)
                    }
                } else {
                    onReadyCallbacks.executeAllAndDestroy()
                    Log.e("LauncherPreviewView", "Model loading failed")
                }
            }
        }
    }

    @UiThread
    private fun renderView(
        inflationContext: Context,
        dataModel: BgDataModel,
        widgetProviderInfoMap: Map<ComponentKey, AppWidgetProviderInfo>?
    ) {
        if (destroyed) {
            return
        }

        val renderer = LauncherPreviewRenderer(inflationContext, idp, null, null, dummyInsets)

        val view = renderer.getRenderedView(dataModel, widgetProviderInfoMap)
        updateScale(view)
        view.pivotX =
            if (layoutDirection == LAYOUT_DIRECTION_RTL) view.measuredWidth.toFloat() else 0f
        view.pivotY = 0f
        view.layoutParams = LayoutParams(view.measuredWidth, view.measuredHeight)
        removeView(spinner)
        rendererView = view
        addView(view)
        onReadyCallbacks.executeAllAndDestroy()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        rendererView?.let { updateScale(it) }
    }

    private fun updateScale(view: View) {
        val scale: Float = min(
            measuredWidth / view.measuredWidth.toFloat(),
            measuredHeight / view.measuredHeight.toFloat()
        )
        view.scaleX = scale
        view.scaleY = scale
    }
}

