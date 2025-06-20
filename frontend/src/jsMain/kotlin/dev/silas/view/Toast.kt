package dev.silas.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.silas.model.TOAST_DURATION
import dev.silas.style.Colors
import kotlinx.coroutines.delay

sealed interface ToastState {
    object Hidden : ToastState
    class Shown(val message: String) : ToastState
}

@Composable
fun Toast(
    state: MutableState<ToastState>
) {
    val value = state.value
    if (value is ToastState.Shown) {
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.size(300.dp, 70.dp),
                color = Colors.ToastBackground,
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(value.message)
                }
                LaunchedEffect(value.message) {
                    delay(TOAST_DURATION)
                    state.value = ToastState.Hidden
                }
            }
        }
    }
}
