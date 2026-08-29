package io.greenstep.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.data.streak.Streak
import io.greenstep.ui.theme.StreakFlameDim
import io.greenstep.ui.theme.ThemeManager

@Composable
fun StreakBadge(
    streak: Streak,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val reduceMotion by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
    val scaleValue: Float = if (reduceMotion) {
        1f
    } else {
        val infinite = rememberInfiniteTransition(label = "flame")
        val s by infinite.animateFloat(
            initialValue = 1f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse),
            label = "flameScale"
        )
        s
    }
    Row(
        modifier = modifier
            .widthIn(max = 160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔥",
            modifier = Modifier.scale(scaleValue),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${streak.current}",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false).padding(start = 6.dp).widthIn(max = 120.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        if (streak.freezes > 0) {
            Box(
                modifier = Modifier.padding(start = 8.dp).size(22.dp).clip(CircleShape).background(StreakFlameDim.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "❄️", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
            }
            Text(
                text = "x${streak.freezes}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp).widthIn(max = 40.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
