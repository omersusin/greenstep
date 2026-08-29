package io.greenstep.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.ui.components.FilizMascot
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onShopClick: () -> Unit = {}) {
    var progress by remember { mutableStateOf(0.42f) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val reduceMotion by ThemeManager.reduceMotionFlow(context).collectAsState(initial = false)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = GreenStepMotion.expressiveSpring,
        label = "ringProgress"
    )
    val scrollState = rememberScrollState()
    LaunchedEffect(animatedProgress) {
        if (animatedProgress >= 1f && !reduceMotion) {
            showConfetti = true
            delay(2200)
            showConfetti = false
        }
    }
    LaunchedEffect(progress) {
        if (progress >= 1f && !reduceMotion) {
            showConfetti = true
            delay(2200)
            showConfetti = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            scope.launch {
                delay(900)
                progress = (progress + 0.12f).coerceAtMost(1f)
                isRefreshing = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.weight(1f)
                )
                StreakBadge(streak = 5, modifier = Modifier.padding(start = 8.dp).widthIn(max = 160.dp))
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                FilizMascot(
                    progress = animatedProgress,
                    modifier = Modifier.size(150.dp)
                )
                if (showConfetti) {
                    ConfettiBurst(modifier = Modifier.fillMaxSize())
                }
            }

            Text(
                text = "${(animatedProgress * 6000).toInt()} / 6000 steps",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(title = "Steps", value = "3,421", modifier = Modifier.weight(1f))
                StatCard(title = "CO₂ saved", value = "0.4 kg", modifier = Modifier.weight(1f))
                StatCard(title = "Coins", value = "128", modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keep growing — every step feeds Filiz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val btnInteraction = remember { MutableInteractionSource() }
                val pressed by btnInteraction.collectIsPressedAsState()
                val btnScale by animateFloatAsState(
                    targetValue = if (pressed) 0.97f else 1f,
                    animationSpec = GreenStepMotion.pressSpring,
                    label = "shopBtnScale"
                )
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onShopClick()
                    },
                    interactionSource = btnInteraction,
                    modifier = Modifier.scale(btnScale)
                ) {
                    Text(
                        text = "Shop",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = GreenStepMotion.pressSpring,
        label = "statPress"
    )
    Card(
        modifier = modifier.scale(scale),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        interactionSource = interaction
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StreakBadge(streak: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.widthIn(max = 160.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).widthIn(max = 160.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔥",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$streak day streak",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.widthIn(max = 120.dp)
            )
        }
    }
}

@Composable
private fun ConfettiBurst(modifier: Modifier = Modifier) {
    val colors = listOf(Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF42A5F5), Color(0xFFFF7043), Color(0xFFAB47BC))
    val particles = remember {
        List(24) { i ->
            Particle(
                xFrac = (i * 0.17f) % 1f,
                yOffset = (i * 0.31f) % 1f,
                size = 6f + (i % 4) * 3f,
                color = colors[i % colors.size],
                speed = 0.4f + (i % 3) * 0.2f,
                isCircle = i % 2 == 0
            )
        }
    }
    val infinite = rememberInfiniteTransition(label = "confetti")
    val drop by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1800), repeatMode = RepeatMode.Restart),
        label = "drop"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            val y = ((drop * p.speed + p.yOffset) % 1f) * h
            val x = p.xFrac * w + kotlin.math.sin((drop * 6 + p.xFrac * 10).toDouble()).toFloat() * 12f
            if (p.isCircle) {
                drawCircle(color = p.color, radius = p.size, center = Offset(x, y))
            } else {
                drawRect(color = p.color, topLeft = Offset(x - p.size, y - p.size), size = androidx.compose.ui.geometry.Size(p.size * 2, p.size * 2))
            }
        }
    }
}

private data class Particle(
    val xFrac: Float,
    val yOffset: Float,
    val size: Float,
    val color: Color,
    val speed: Float,
    val isCircle: Boolean
)
