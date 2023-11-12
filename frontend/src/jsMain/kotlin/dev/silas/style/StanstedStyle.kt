package dev.silas.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@Composable
fun StanstedTheme(content: @Composable () -> Unit) {
    isSystemInDarkTheme() // todo check and change colors
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            background = Colors.background,
            onBackground = Colors.onBackground
        )
    ) {
        ProvideTextStyle(LocalTextStyle.current.copy(letterSpacing = 0.sp)) {
            content()
        }
    }
}
