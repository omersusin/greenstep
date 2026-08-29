package io.greenstep.ui.challenge

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.data.challenge.Challenge
import io.greenstep.data.challenge.ChallengeDuration
import io.greenstep.data.challenge.ChallengeType
import io.greenstep.data.challenge.ecoAdventureCheckpoints
import io.greenstep.data.challenge.sampleEcoAdventure
import io.greenstep.ui.theme.GreenStepMotion

@Composable
fun ChallengeScreen(
    challenges: List<Challenge> = sampleChallenges,
    onJoin: (Challenge) -> Unit = {}
) {
    var selected by remember { mutableStateOf<ChallengeType?>(null) }
    var joinedIds by remember { mutableStateOf(setOf<String>()) }
    val filtered = if (selected == null) challenges else challenges.filter { it.type == selected }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { WeeklyChallengeBanner() }
        item { EcoAdventureHero(progress = sampleEcoAdventure.progress, joined = joinedIds.contains(sampleEcoAdventure.id), onJoin = { joinedIds = if (joinedIds.contains(sampleEcoAdventure.id)) joinedIds - sampleEcoAdventure.id else joinedIds + sampleEcoAdventure.id }) }
        item { ChallengeFilterBar(selected = selected, onSelect = { selected = it }) }
        items(filtered, key = { it.id }) { c ->
            val isJoined = joinedIds.contains(c.id)
            ChallengeCard(challenge = c, joined = isJoined, onJoin = {
                joinedIds = if (isJoined) joinedIds - c.id else joinedIds + c.id
                onJoin(c)
            })
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun WeeklyChallengeBanner() {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🌱", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Weekly Challenge", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth())
                Text(text = "Walk 50K steps together — 3 days left! Filiz is cheering 🌿", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ChallengeFilterBar(selected: ChallengeType?, onSelect: (ChallengeType?) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All", maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) })
        ChallengeType.entries.forEach { t ->
            FilterChip(selected = selected == t, onClick = { onSelect(if (selected == t) null else t) }, label = { Text(t.label, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 120.dp)) })
        }
    }
}

@Composable
private fun EcoAdventureHero(progress: Float, joined: Boolean, onJoin: () -> Unit) {
    val animated by animateFloatAsState(targetValue = progress, animationSpec = GreenStepMotion.expressiveSpringSpec(), label = "ecoProg")
    val haptic = LocalHapticFeedback.current
    var certVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (joined) 1.02f else 1f, animationSpec = GreenStepMotion.expressiveSpringSpec(), label = "ecoScale")
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).scale(scale), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = "🌎 Eco Adventure", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text("4 wks", maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) }, colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.widthIn(max = 100.dp))
            }
            Text(text = "Amazon Trail 120 km → plant 10 trees", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
            Text(text = "Walk the rainforest route — checkpoints, postcards & a certificate at the finish!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    CircularProgressIndicator(progress = { animated }, modifier = Modifier.fillMaxWidth(), strokeWidth = 6.dp, color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surface)
                    Text(text = "${(animated * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    LinearProgressIndicator(progress = { animated }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)))
                    Text(text = "${(animated * 120).toInt()} / 120 km • ${(animated * 10).toInt()} / 10 trees", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                }
                Button(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onJoin()
                    if (!joined) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }, modifier = Modifier.widthIn(max = 120.dp)) {
                    Text(text = if (joined) "Joined ✓" else "Join", maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ecoAdventureCheckpoints.forEachIndexed { idx, cp ->
                    val reached = animated * 120f >= cp.km
                    Card(colors = CardDefaults.cardColors(containerColor = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface), modifier = Modifier.width(96.dp)) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = cp.emoji, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                            Text(text = cp.title, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth(), color = if (reached) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            Text(text = "${cp.km.toInt()} km", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (reached) Text(text = "✓", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "📮 Postcard", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                        Text(text = "Canopy Camp — sent by Filiz", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(modifier = Modifier.weight(1f).clickable { certVisible = !certVisible }, colors = CardDefaults.cardColors(containerColor = if (animated >= 1f) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (animated >= 1f) "🏆 Certificate" else "🔒 Certificate", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth())
                        Text(text = if (certVisible) "Amazon Guardian — Filiz & you" else "Finish to unlock", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            ParticipantAvatars(count = 892)
        }
    }
}

@Composable
private fun ChallengeCard(challenge: Challenge, joined: Boolean, onJoin: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(targetValue = if (joined) 1.02f else 1f, animationSpec = GreenStepMotion.expressiveSpringSpec(), label = "joinScale")
    var expanded by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(targetValue = challenge.progress.coerceIn(0f, 1f), animationSpec = GreenStepMotion.gentleSpringSpec(), label = "prog")
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).scale(scale), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(14.dp).fillMaxWidth().animateContentSize()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = iconForType(challenge.type), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = challenge.type.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 100.dp))
                Spacer(Modifier.width(6.dp))
                AssistChip(onClick = {}, label = { Text(challenge.duration.label, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) }, modifier = Modifier.widthIn(max = 80.dp))
                Spacer(Modifier.weight(1f))
                Text(text = "${challenge.daysLeft} days left", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 110.dp))
            }
            Text(text = challenge.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            if (challenge.description.isNotEmpty()) Text(text = challenge.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                    CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(44.dp), strokeWidth = 4.dp, color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surface)
                    Text(text = "${(animatedProgress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)))
                    Text(text = "+${challenge.rewardCoins} coins", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.padding(top = 4.dp))
                }
                Button(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onJoin()
                    if (!joined) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }, colors = if (joined) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors(), modifier = Modifier.widthIn(max = 120.dp)) {
                    Text(text = if (joined) "Joined" else "Join", maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, fontWeight = FontWeight.Bold)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                ParticipantAvatars(count = challenge.participants)
                Spacer(Modifier.weight(1f))
                Text(text = if (expanded) "▼ Leaderboard" else "▶ Leaderboard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.clickable { expanded = !expanded }.widthIn(max = 140.dp))
            }
            if (expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("🥇 Ava — 42 km", "🥈 Leo — 38 km", "🥉 Filiz — 34 km", "4. You — ${(animatedProgress * 40).toInt()} km").forEach { line ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = line, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.weight(1f))
                            Text(text = "cheer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 60.dp))
                        }
                    }
                    Text(text = "Live — updates every hour", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ParticipantAvatars(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.height(28.dp).width(56.dp)) {
            listOf("A", "B", "C").forEachIndexed { idx, ini ->
                Box(modifier = Modifier.offset(x = (idx * 18).dp).size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary).border(2.dp, MaterialTheme.colorScheme.surface, CircleShape), contentAlignment = Alignment.Center) {
                    Text(text = ini, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(text = "$count participants", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 140.dp))
    }
}

private fun iconForType(type: ChallengeType): ImageVector = when (type) {
    ChallengeType.STEP -> Icons.Outlined.DirectionsRun
    ChallengeType.DAILY_GOAL -> Icons.Outlined.Flag
    ChallengeType.DISTANCE -> Icons.Outlined.Terrain
    ChallengeType.ECO_ADVENTURE -> Icons.Outlined.Eco
}

private val sampleChallenges = listOf(
    Challenge(id = "1", title = "Green Sprint — 10K daily steps for 7 days straight", type = ChallengeType.DAILY_GOAL, duration = ChallengeDuration.ONE, daysLeft = 5, progress = 0.42f, participants = 342, rewardCoins = 150, description = "Hit your daily goal 7 days in a row ✨"),
    Challenge(id = "2", title = "Forest Guardian — Plant 5 virtual trees together", type = ChallengeType.STEP, duration = ChallengeDuration.TWO, daysLeft = 9, progress = 0.78f, participants = 1289, rewardCoins = 200, description = "50K steps = 1 tree — Filiz waters them 🌱"),
    Challenge(id = "3", title = "Weekend Explorer Challenge with very long title to test ellipsis handling properly", type = ChallengeType.DISTANCE, duration = ChallengeDuration.ONE, daysLeft = 2, progress = 0.15f, participants = 512, rewardCoins = 80),
    Challenge(id = "4", title = "City Loop 30 km — Discover green streets", type = ChallengeType.DISTANCE, duration = ChallengeDuration.THREE, daysLeft = 14, progress = 0.55f, participants = 623, rewardCoins = 300, description = "Log 30 km via GPS or steps"),
    Challenge(id = "5", title = "Streak Keeper — 14-day daily goal", type = ChallengeType.DAILY_GOAL, duration = ChallengeDuration.TWO, daysLeft = 10, progress = 0.62f, participants = 445, rewardCoins = 180)
)
