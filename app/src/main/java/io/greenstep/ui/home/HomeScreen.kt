package io.greenstep.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import android.content.Intent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.greenstep.R
import io.greenstep.ui.components.FilizMascot
import io.greenstep.ui.components.StreakBadge
import io.greenstep.ui.components.rememberHaptics
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onShopClick: () -> Unit = {}, onChallengesClick: () -> Unit = {}, onFeedClick: () -> Unit = {}, viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val day = uiState.day
    val streak = uiState.streak
    val yesterdaySteps = uiState.yesterdaySteps
    val progress = (day.steps.toFloat() / day.goal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1.5f)
    val clampedProgress = progress.coerceIn(0f, 1f)
    var isRefreshing by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val context = LocalContext.current
    val reduceMotion by ThemeManager.reduceMotionFlow(context).collectAsState(initial = false)
    val animatedProgress by animateFloatAsState(targetValue = clampedProgress, animationSpec = GreenStepMotion.expressiveSpring, label = "ringProgress")
    val scrollState = rememberScrollState()
    LaunchedEffect(animatedProgress) {
        if (animatedProgress >= 1f && !reduceMotion) {
            showConfetti = true
            delay(2200)
            showConfetti = false
        }
    }
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = {
        isRefreshing = true
        haptics.tick()
        scope.launch { delay(900); isRefreshing = false; haptics.success() }
    }, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Today", style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.weight(1f))
                StreakBadge(streak = streak, modifier = Modifier.padding(start = 8.dp).widthIn(max = 160.dp))
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                SegmentedRing(progress = animatedProgress, modifier = Modifier.fillMaxSize())
                val memory = yesterdaySteps?.let { if (it >= 8000) "Remember yesterday's ${it} walk?" else null }
                FilizMascot(progress = animatedProgress, modifier = Modifier.size(150.dp), memoryLine = memory)
                if (showConfetti) ConfettiBurst(modifier = Modifier.fillMaxSize())
            }
            Text(text = "${day.steps} / ${day.goal} steps", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "min ${day.goal / 2}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "target ${day.goal}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "stretch ${(day.goal * 1.5f).toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            val dist = String.format("%.2f km", day.distanceKm)
            val cal = "${day.calories.toInt()} kcal"
            val co2 = String.format("%.2f kg", day.carbonSaved)
            val coins = "${day.coins}"
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(title = "Distance", value = dist, modifier = Modifier.weight(1f))
                StatCard(title = "Calories", value = cal, modifier = Modifier.weight(1f))
                StatCard(title = "CO₂ saved", value = co2, modifier = Modifier.weight(1f))
                StatCard(title = "Coins", value = coins, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Keep growing — every step feeds Filiz.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                val btnInteraction = remember { MutableInteractionSource() }
                val pressed by btnInteraction.collectIsPressedAsState()
                val btnScale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = GreenStepMotion.pressSpring, label = "shopBtnScale")
                Button(onClick = { haptics.success(); onShopClick() }, interactionSource = btnInteraction, modifier = Modifier.scale(btnScale)) {
                    Text(text = "Shop", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            ShareImpactCard(steps = day.steps, co2 = day.carbonSaved)
            WeeklyWrappedCard(viewModel = viewModel)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SegmentedRing(progress: Float, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val minColor = primary.copy(alpha = 0.35f)
    val targetColor = primary.copy(alpha = 0.7f)
    Canvas(modifier = modifier) {
        val stroke = 12.dp.toPx()
        val diameter = size.minDimension - stroke
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val arcSize = Size(diameter, diameter)
        drawArc(color = surfaceVariant, startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Round))
        if (progress > 0f) {
            val seg1 = (progress / 0.5f).coerceIn(0f, 1f) * 120f
            drawArc(color = minColor, startAngle = -90f, sweepAngle = seg1, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
        if (progress > 0.5f) {
            val seg2 = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f) * 120f
            drawArc(color = targetColor, startAngle = 30f, sweepAngle = seg2, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
        if (progress >= 1f) {
            drawArc(color = tertiary, startAngle = 150f, sweepAngle = 120f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = rememberHaptics()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = GreenStepMotion.pressSpring, label = "statPress")
    Card(modifier = modifier.scale(scale), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), onClick = { haptics.light() }, interactionSource = interaction) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ConfettiBurst(modifier: Modifier = Modifier) {
    val colors = listOf(Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF42A5F5), Color(0xFFFF7043), Color(0xFFAB47BC))
    val particles = remember { List(24) { i -> Particle(xFrac = (i * 0.17f) % 1f, yOffset = (i * 0.31f) % 1f, size = 6f + (i % 4) * 3f, color = colors[i % colors.size], speed = 0.4f + (i % 3) * 0.2f, isCircle = i % 2 == 0) } }
    val infinite = rememberInfiniteTransition(label = "confetti")
    val drop by infinite.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(1800), repeatMode = RepeatMode.Restart), label = "drop")
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        particles.forEach { p ->
            val y = ((drop * p.speed + p.yOffset) % 1f) * h
            val x = p.xFrac * w + kotlin.math.sin((drop * 6 + p.xFrac * 10).toDouble()).toFloat() * 12f
            if (p.isCircle) drawCircle(color = p.color, radius = p.size, center = Offset(x, y))
            else drawRect(color = p.color, topLeft = Offset(x - p.size, y - p.size), size = Size(p.size * 2, p.size * 2))
        }
    }
}

private data class Particle(val xFrac: Float, val yOffset: Float, val size: Float, val color: Color, val speed: Float, val isCircle: Boolean)

@Composable
private fun ShareImpactCard(steps: Int, co2: Float) {
    val context = LocalContext.current
    val haptics = rememberHaptics()
    val shareText = stringResource(R.string.home_share_text, steps, co2)
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(text = stringResource(R.string.home_impact_title), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = stringResource(R.string.home_impact_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            val inter = remember { MutableInteractionSource() }
            val pressed by inter.collectIsPressedAsState()
            val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = GreenStepMotion.pressSpring, label = "shareScale")
            Button(onClick = {
                haptics.success()
                val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.home_share_chooser)))
            }, interactionSource = inter, modifier = Modifier.scale(scale)) {
                Text(text = stringResource(R.string.home_share_progress), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun WeeklyWrappedCard(viewModel: HomeViewModel) {
    val weekly by viewModel.weeklyDays.collectAsState()
    val total = weekly.sumOf { it.steps }
    val avg = if (weekly.isEmpty()) 0 else total / weekly.size
    val co2 = weekly.sumOf { it.carbonSaved.toDouble() }.toFloat()
    val best = weekly.maxOfOrNull { it.steps } ?: 0
    if (weekly.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.home_wrapped_title), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = stringResource(R.string.home_wrapped_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.home_wrapped_total_steps, total), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = stringResource(R.string.home_wrapped_avg, avg), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.home_wrapped_co2, co2), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = stringResource(R.string.home_wrapped_best, best), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
