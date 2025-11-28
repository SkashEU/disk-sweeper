package com.skash.sweeper.designsystem.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.skash.sweeper.designsystem.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Screen(
    modifier: Modifier = Modifier,
    title: String? = null,
    subTitle: String? = null,
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentWindowInsets: WindowInsets = WindowInsets(
        left = Spacing.ExtraLarge,
        right = Spacing.ExtraLarge,
        top = Spacing.Large,
        bottom = Spacing.Large
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = Modifier.then(modifier),
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        contentWindowInsets = contentWindowInsets
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            title?.let {
                Column(modifier = Modifier.padding(bottom = Spacing.Large)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLargeEmphasized
                    )

                    subTitle?.let {
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            content()
        }
    }
}