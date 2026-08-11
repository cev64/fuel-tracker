package com.personal.fuel.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.personal.fuel.ui.theme.FuelTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * The accent pill confirmation from the web app. Deliberately not a Snackbar:
 * it is non-interactive, does not push layout around, and disappears on its own.
 */
@Composable
fun FuelToastHost(
    messages: Flow<String>,
    modifier: Modifier = Modifier,
) {
    var current by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(messages) {
        messages.collect { message ->
            current = message
            delay(1_800)
            current = null
        }
    }

    val message = current
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            initialOffsetY = { it / 2 },
        ),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(FuelTheme.colors.accent)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(
                text = message.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = FuelTheme.colors.onAccent,
            )
        }
    }
}
