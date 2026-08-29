package io.greenstep.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.greenstep.R
import io.greenstep.ui.theme.ThemeManager
import kotlinx.coroutines.delay

@Composable
fun FilizMascot(
    progress: Float,
    modifier: Modifier = Modifier,
    sizeDp: Int = 160,
    onTap: (() -> Unit)? = null
) {
    val drawable = when {
        progress >= 1f -> R.drawable.filiz_tree
        progress >= 0.5f -> R.drawable.filiz_plant
        progress >= 0.25f -> R.drawable.filiz_sprout
        else -> R.drawable.filiz_seed
    }
    var bounced by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val ctx = LocalContext.current
    val reduceMotion by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
    val tapScale by animateFloatAsState(
        targetValue = if (bounced) 1.18f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "tapScale"
    )
    val infinite = rememberInfiniteTransition(label = "breathing")
    val breath by infinite.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(animation = tween(1800), repeatMode = RepeatMode.Reverse),
        label = "breath"
    )
    val wilt by infinite.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "wilt"
    )
    val breathScale = if (reduceMotion) 1f else breath
    val wiltRotation = if (progress < 0.2f && !reduceMotion) wilt else 0f
    val combinedScale = tapScale * breathScale
    LaunchedEffect(bounced) {
        if (bounced) {
            delay(180)
            bounced = false
        }
    }
    Image(
        painter = painterResource(drawable),
        contentDescription = stringResource(R.string.filiz_dialog_title),
        modifier = modifier
            .size(sizeDp.dp)
            .scale(combinedScale)
            .graphicsLayer(rotationZ = wiltRotation)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                bounced = true
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTap?.invoke()
            }
    )
}
