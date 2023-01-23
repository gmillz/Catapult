package app.catapult.launcher.settings.ui.components

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.extensions.addIf
import com.gmillz.compose.settings.ui.components.NestedScrollStretch
import com.gmillz.compose.settings.ui.components.SettingDivider
import com.gmillz.compose.settings.ui.components.SettingGroupHeader
import com.google.accompanist.flowlayout.FlowColumn
import kotlinx.coroutines.awaitCancellation

@Composable
fun SettingsLazyColumn(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    state: LazyListState = rememberLazyListState(),
    isChild: Boolean = false,
    content: LazyListScope.() -> Unit
) {
   if (!enabled) {
       LaunchedEffect(key1 = null) {
           state.scroll(scrollPriority = MutatePriority.PreventUserInput) {
               awaitCancellation()
           }
       }
   }
        NestedScrollStretch {
            LazyColumn(
                modifier = modifier
                    .addIf(isChild) {
                        fillMaxHeight()
                    },
                content = content
            )
        }
}

fun LazyListScope.settingGroupItems(
    count: Int,
    title: (@Composable () -> String)? = null,
    key: ((index: Int) -> Any)? = null,
    contentType: (index: Int) -> Any? = { null },
    itemContent: @Composable LazyItemScope.(index: Int) -> Unit
) {
    item {
        SettingGroupHeader(title = title?.let { it() })
    }
    items(count, key, contentType) {
        SettingGroupItem(cutTop = it > 0, cutBottom = it < count -1) {
            if (it > 0) {
                SettingDivider()
            }
            itemContent(it)
        }
    }
}

inline fun <T> LazyListScope.settingGroupItems(
    items: List<T>,
    noinline title: (@Composable () -> String)? = null,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (index: Int) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    settingGroupItems(
        count = items.size,
        title = title,
        key = if (key != null) { index -> key(items[index]) } else null,
        contentType = contentType,
        itemContent = { index -> itemContent(items[index]) }
    )
}

@Composable
fun SettingGroupItem(
    modifier: Modifier = Modifier,
    cutTop: Boolean = false,
    cutBottom: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = remember(cutTop, cutBottom) {
        val top = if (cutTop) 0.dp else 12.dp
        val bottom = if (cutBottom) 0.dp else 12.dp
        RoundedCornerShape(top, top, bottom, bottom)
    }
    Surface(
        modifier = modifier.padding(horizontal = 16.dp),
        shape = shape,
        tonalElevation = 1.dp
    ) {
        content()
    }
}