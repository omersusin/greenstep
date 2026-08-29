package io.greenstep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.greenstep.ui.theme.Green300
import io.greenstep.ui.theme.Green700

@Composable
fun HeatmapView(
    steps: List<Int>,
    modifier: Modifier = Modifier,
    columns: Int = 7,
    rows: Int = 5,
    goal: Int? = null,
    contentDesc: String? = null,
) {
    val total = columns * rows
    val data = when {
        steps.size >= total -> steps.take(total)
        else -> steps + List(total - steps.size) { 0 }
    }
    val max = (data.maxOrNull() ?: 0).coerceAtLeast(1)
    val goalRef = goal?.coerceAtLeast(1)
    val minCellColor = MaterialTheme.colorScheme.surfaceVariant
    val maxCellColor = Green700
    val midCellColor = Green300
    Column(modifier = modifier.fillMaxWidth().then(if (contentDesc != null) Modifier.semantics { contentDescription = contentDesc } else Modifier), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in 0 until columns) {
                    val idx = r * columns + c
                    val value = data[idx]
                    val fraction = if (goalRef != null) (value.toFloat() / goalRef.toFloat()).coerceIn(0f, 1f) else value.toFloat() / max.toFloat()
                    val color: Color = when {
                        fraction <= 0f -> minCellColor
                        fraction < 0.5f -> lerp(minCellColor, midCellColor, fraction * 2f)
                        else -> lerp(midCellColor, maxCellColor, (fraction - 0.5f) * 2f)
                    }
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(color))
                }
            }
        }
    }
}

@Composable
fun HeatmapViewDays(stepsByDay: List<Int>, modifier: Modifier = Modifier) {
    HeatmapView(steps = stepsByDay, modifier = modifier)
}
