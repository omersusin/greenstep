package io.greenstep.ui.social

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.data.economy.CoinStore
import io.greenstep.ui.components.rememberHaptics
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class FeedPrivacy(val label: String) { PUBLIC("Public"), CLUB("Club"), PRIVATE("Private") }

data class FeedPost(val id: String, val author: String, val initials: String, val club: String, val text: String, val privacy: FeedPrivacy = FeedPrivacy.PUBLIC, val cheers: Int = 12, val clubSupport: Boolean = false)

@Composable
fun FeedScreen(
    posts: List<FeedPost> = samplePosts,
    onCheer: (FeedPost) -> Unit = {}
) {
    var filter by remember { mutableStateOf("All") }
    var privacyFilter by remember { mutableStateOf<FeedPrivacy?>(null) }
    var localPosts by remember { mutableStateOf(posts) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val coinStore = remember { CoinStore(context) }
    val haptic = rememberHaptics()
    val snackbar = remember { SnackbarHostState() }
    val filtered = localPosts.filter {
        val okFilter = when (filter) {
            "Club" -> it.clubSupport || it.privacy == FeedPrivacy.CLUB
            "Following" -> it.author != "You"
            else -> true
        }
        val okPrivacy = privacyFilter == null || it.privacy == privacyFilter
        okFilter && okPrivacy
    }
    LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Following", "Club").forEach { f ->
                    FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) })
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Privacy:", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                FilterChip(selected = privacyFilter == null, onClick = { privacyFilter = null }, label = { Text("Any", maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) })
                FeedPrivacy.entries.forEach { p ->
                    FilterChip(selected = privacyFilter == p, onClick = { privacyFilter = if (privacyFilter == p) null else p }, label = { Text(p.label, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 90.dp)) })
                }
            }
        }
        items(filtered, key = { it.id }) { post ->
            FeedPostCard(post = post, onCheer = {
                val bonus = Random.nextInt(2, 7)
                scope.launch { coinStore.addCoins(bonus) }
                localPosts = localPosts.map { if (it.id == post.id) it.copy(cheers = it.cheers + 1, clubSupport = true) else it }
                haptic.success()
                onCheer(post)
            }, onShare = { haptic.tick() })
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun FeedPostCard(post: FeedPost, onCheer: () -> Unit, onShare: () -> Unit) {
    val haptic = rememberHaptics()
    val ctx = LocalContext.current
    val reduceMotion by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
    var cheered by remember { mutableStateOf(false) }
    var showShareCard by remember { mutableStateOf(false) }
    var sparkle by remember { mutableStateOf(false) }
    LaunchedEffect(cheered) { if (cheered) { sparkle = true; delay(600); sparkle = false } }
    val cheerScale by animateFloatAsState(targetValue = if (cheered && !reduceMotion) 1.02f else 1f, animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.expressiveSpring, label = "cheerScale")
    val pressInteraction = remember { MutableInteractionSource() }
    val pressed by pressInteraction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label = "pressScale")
    val combinedScale = cheerScale * pressScale * if (sparkle && !reduceMotion) 1.02f else 1f
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).scale(combinedScale), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Text(text = post.initials, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = post.author, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.weight(1f, fill = false).widthIn(max = 120.dp))
                        Icon(imageVector = when (post.privacy) { FeedPrivacy.PUBLIC -> Icons.Outlined.Public; FeedPrivacy.CLUB -> Icons.Outlined.Group; FeedPrivacy.PRIVATE -> Icons.Outlined.Lock }, contentDescription = null, modifier = Modifier.size(14.dp).padding(start = 4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = post.club, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.weight(1f, fill = false).widthIn(max = 140.dp))
                        Text(text = " • 2h ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                    }
                }
                AssistChip(onClick = {}, label = { Text(post.privacy.label, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) }, modifier = Modifier.widthIn(max = 90.dp))
            }
            Text(text = post.text, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            if (post.clubSupport) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(text = "Club support +${post.club} cheered!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.weight(1f))
                    }
                }
            }
            if (showShareCard) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Text(text = "🌿 Share Card", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth())
                        Text(text = "${post.author} • ${post.text}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth())
                        Text(text = "GreenStep • Filiz approves 🌱", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${post.cheers} cheers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.weight(1f).widthIn(max = 100.dp))
                IconButton(onClick = { showShareCard = !showShareCard; onShare() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(4.dp))
                val cheerInteraction = remember { MutableInteractionSource() }
                val cheerPressed by cheerInteraction.collectIsPressedAsState()
                val cheerBtnScale by animateFloatAsState(targetValue = if (cheerPressed) 0.97f else 1f, animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label = "cheerBtn")
                Button(onClick = {
                    haptic.tick()
                    cheered = !cheered
                    if (cheered) onCheer() else haptic.success()
                }, interactionSource = cheerInteraction, colors = if (cheered) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors(), modifier = Modifier.widthIn(max = 150.dp).scale(cheerBtnScale)) {
                    Text(text = if (cheered) "Cheered! +coins" else "Cheer 🎉", maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 120.dp))
                }
            }
            if (cheered) Text(text = "Variable reward: +${(2..6).random()} bonus coins!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))
        }
    }
}

private val samplePosts = listOf(
    FeedPost(id = "1", author = "Anna", initials = "A", club = "Forest Runners 🌲", text = "Anna planted Oak — 8,432 steps today", privacy = FeedPrivacy.PUBLIC, cheers = 24, clubSupport = true),
    FeedPost(id = "2", author = "Ben", initials = "B", club = "City Sprinters", text = "Ben completed Forest Guardian — 12,104 steps with extra long title to verify ellipsis", privacy = FeedPrivacy.CLUB, cheers = 18),
    FeedPost(id = "3", author = "Clara", initials = "C", club = "Meadow Walkers", text = "Clara joined Green Sprint — 5,210 steps 🌱 Filiz is proud", privacy = FeedPrivacy.PUBLIC, cheers = 9, clubSupport = true),
    FeedPost(id = "4", author = "David", initials = "D", club = "Eco Crew", text = "David planted Pine — 9,876 steps • Amazon Trail 12 km", privacy = FeedPrivacy.PRIVATE, cheers = 31),
    FeedPost(id = "5", author = "Filiz", initials = "F", club = "GreenStep HQ", text = "Filiz: 10 trees unlocked! Cheers give bonus coins 🎲", privacy = FeedPrivacy.PUBLIC, cheers = 102, clubSupport = true)
)
