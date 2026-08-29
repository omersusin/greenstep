package io.greenstep.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.ui.components.rememberHaptics
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ThemeManager
import androidx.lifecycle.viewmodel.compose.viewModel
import io.greenstep.R
import io.greenstep.data.day.Day
import io.greenstep.ui.components.HeatmapView
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = viewModel()) {
    val days by viewModel.days.collectAsState()
    HistoryContent(days = days)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HistoryContent(days: List<Day>) {
    if (days.isEmpty()) {
        HistoryEmptyState(modifier = Modifier.fillMaxSize().padding(32.dp))
        return
    }
    val stepsList = remember(days) { days.map { it.steps } }
    val avgGoal = remember(days) { if (days.isEmpty()) 7500 else days.map { it.goal }.average().toInt().coerceAtLeast(1) }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(key = "heatmap") {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(R.string.history_heatmap_title), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                    Text(text = stringResource(R.string.history_heatmap_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    HeatmapView(steps = stepsList, goal = avgGoal, columns = 7, rows = 5, contentDesc = stringResource(R.string.history_heatmap_title))
                }
            }
        }
        items(days, key = { it.date.toEpochDay() }) { day ->
            HistoryDayCard(day = day, modifier = Modifier)
        }
    }
}

@Composable
private fun HistoryDayCard(day: Day, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val reduce by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = rememberHaptics()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = if (reduce) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label = "histCard")
    Card(shape = RoundedCornerShape(24.dp), modifier = modifier.fillMaxWidth().scale(scale), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), onClick = { haptic.tick() }, interactionSource = interaction) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = day.date.toString(), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.weight(1f).padding(end = 12.dp))
            Text(text = stringResource(R.string.history_day_steps, day.steps), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
        }
    }
}

@Composable
private fun HistoryEmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painter = painterResource(R.drawable.filiz_sleeping), contentDescription = stringResource(R.string.history_empty_title), modifier = Modifier.size(140.dp))
        Spacer(Modifier.height(20.dp))
        Text(text = stringResource(R.string.history_empty_title), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, softWrap = false, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        Text(text = stringResource(R.string.history_empty_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        Spacer(Modifier.height(12.dp))
        Text(text = "🌱 Filiz schnarcht leise — weck sie mit Schritten!", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    }
}
