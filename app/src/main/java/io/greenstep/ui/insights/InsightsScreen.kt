package io.greenstep.ui.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.greenstep.R
import io.greenstep.ui.components.HeatmapView
import io.greenstep.ui.theme.ThemeManager
import java.text.NumberFormat
import java.util.Locale

@Composable
fun InsightsScreen(viewModel: InsightsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    InsightsContent(stepsLast14 = uiState.stepsLast14, todaySteps = uiState.todaySteps, weeklyAvg = uiState.weeklyAvg, bestDaySteps = uiState.bestDaySteps, bestDayLabel = uiState.bestDayLabel)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun InsightsContent(stepsLast14: List<Int>, todaySteps: Int, weeklyAvg: Int, bestDaySteps: Int, bestDayLabel: String) {
    val nf = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val ctx = LocalContext.current
    val reduceMotion by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
    if (stepsLast14.isEmpty() || stepsLast14.all { it == 0 }) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(painter = painterResource(R.drawable.filiz_sleeping), contentDescription = stringResource(R.string.insights_empty), modifier = Modifier.size(140.dp))
            Spacer(Modifier.height(16.dp))
            Text(text = stringResource(R.string.insights_empty), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(key = "summary") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(title = stringResource(R.string.insights_summary_today), value = nf.format(todaySteps), modifier = Modifier.weight(1f))
                SummaryCard(title = stringResource(R.string.insights_summary_weekly_avg), value = nf.format(weeklyAvg), modifier = Modifier.weight(1f))
                SummaryCard(title = stringResource(R.string.insights_summary_best_day), value = nf.format(bestDaySteps), subtitle = bestDayLabel, modifier = Modifier.weight(1f))
            }
        }
        item(key = "sparkline") {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(R.string.insights_trend_title), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                    Text(text = stringResource(R.string.insights_trend_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Sparkline(values = stepsLast14, animate = !reduceMotion, modifier = Modifier.fillMaxWidth().height(80.dp))
                }
            }
        }
        item(key = "heatmap") {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(R.string.insights_heatmap_title), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                    HeatmapView(steps = stepsLast14, columns = 7, rows = 2)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Card(shape = RoundedCornerShape(24.dp), modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
            Text(text = value, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
            if (!subtitle.isNullOrEmpty()) Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
        }
    }
}

@Composable
private fun Sparkline(values: List<Int>, modifier: Modifier = Modifier, animate: Boolean = true) {
    val primary = MaterialTheme.colorScheme.primary
    val progress by animateFloatAsState(targetValue = 1f, animationSpec = if (animate) tween(1200) else tween(0), label = "sparkDraw")
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val max = (values.maxOrNull() ?: 1).coerceAtLeast(1).toFloat()
        val min = (values.minOrNull() ?: 0).toFloat()
        val range = (max - min).coerceAtLeast(1f)
        val stepX = size.width / (values.size - 1).coerceAtLeast(1)
        val fullPath = Path().apply {
            values.forEachIndexed { i, v ->
                val x = i * stepX
                val y = size.height - ((v - min) / range * size.height)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        val pm = PathMeasure()
        pm.setPath(fullPath, false)
        val seg = Path()
        pm.getSegment(0f, pm.length * progress, seg, true)
        drawPath(path = seg, color = primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        values.forEachIndexed { i, v ->
            if (i.toFloat() / (values.size - 1) > progress) return@forEachIndexed
            val x = i * stepX
            val y = size.height - ((v - min) / range * size.height)
            drawCircle(color = primary, radius = 4.dp.toPx(), center = Offset(x, y))
        }
    }
}
