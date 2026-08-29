package io.greenstep.ui.social

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.ui.theme.GreenStepMotion

data class FeedPost(val id: String, val text: String, val initials: String)

@Composable
fun FeedScreen(
    posts: List<FeedPost> = samplePosts,
    onCheer: (FeedPost) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            FeedPostCard(post = post, onCheer = { onCheer(post) })
        }
    }
}

@Composable
private fun FeedPostCard(
    post: FeedPost,
    onCheer: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var cheered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (cheered) 1.02f else 1f,
        animationSpec = GreenStepMotion.expressiveSpringSpec(),
        label = "cheerScale"
    )
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).scale(scale),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.initials,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(
                    text = post.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "2h ago • GreenStep community",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    cheered = !cheered
                    onCheer()
                    if (cheered) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                modifier = Modifier.widthIn(max = 120.dp)
            ) {
                Text(
                    text = if (cheered) "Cheered!" else "Cheer",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
        }
    }
}

private val samplePosts = listOf(
    FeedPost(id = "1", text = "Anna planted Oak — 8,432 steps", initials = "A"),
    FeedPost(id = "2", text = "Ben completed Forest Guardian — 12,104 steps with extra long title to verify ellipsis", initials = "B"),
    FeedPost(id = "3", text = "Clara joined Green Sprint — 5,210 steps", initials = "C"),
    FeedPost(id = "4", text = "David planted Pine — 9,876 steps", initials = "D")
)
