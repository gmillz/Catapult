package app.catapult.launcher.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.gmillz.compose.settings.ui.components.BottomSpacer
import com.gmillz.compose.settings.util.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScaffold(
    backArrowVisible: Boolean = true,
    label: String,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = { BottomSpacer() },
    content: @Composable (PaddingValues) -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = label) },
                scrollBehavior = scrollBehavior,
                actions = actions,
                navigationIcon = {
                    if (backArrowVisible) {
                        Icon(
                            modifier = Modifier
                                .clickable {
                                    navController.navigateUp()
                                },
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = bottomBar,
    ) {
        content(it)
    }
}