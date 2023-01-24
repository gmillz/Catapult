package app.catapult.launcher.settings.ui.components

import android.app.Activity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.extensions.addIf
import com.gmillz.compose.settings.ui.components.BottomSpacer
import com.gmillz.compose.settings.ui.components.SettingsToolbar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsLayout(
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrollState: ScrollState? = rememberScrollState(),
    label: String,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = { BottomSpacer() },
    backArrowVisible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            SettingsToolbar(
                title = { Text(text = label) },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                onBack = if (!backArrowVisible) null else {{
                    if (context is Activity) {
                        context.finishAndRemoveTask()
                    }
                }},
                actions = actions
            )
        },
        bottomBar = bottomBar
    ) { innerPadding ->
        Column(
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            modifier = Modifier
                .consumedWindowInsets(innerPadding)
                .fillMaxHeight()
                .addIf(scrollState != null) {
                    verticalScroll(scrollState!!)
                }
                .padding(top = innerPadding.calculateTopPadding(), bottom = 16.dp),
            content = content
        )
    }
}