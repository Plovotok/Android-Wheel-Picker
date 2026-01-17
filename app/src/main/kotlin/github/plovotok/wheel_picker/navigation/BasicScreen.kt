package github.plovotok.wheel_picker.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicScreen(
    modifier: Modifier = Modifier,
    title: String,
    isLarge: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val behavior =
        if (isLarge) TopAppBarDefaults.exitUntilCollapsedScrollBehavior() else TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            if (isLarge) {
                MediumTopAppBar(
                    title = {
                        Text(
                            text = title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = navigationIcon,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    scrollBehavior = behavior
                )
            } else {
                TopAppBar(
                    scrollBehavior = behavior,
                    title = {
                        Text(
                            text = title
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    navigationIcon = navigationIcon
                )
            }
        },
        modifier = modifier.fillMaxSize(),
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(behavior.nestedScrollConnection)
                .then(
                    if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
                )
                .padding(paddingValues)
        ) {
            content()
        }
    }
}