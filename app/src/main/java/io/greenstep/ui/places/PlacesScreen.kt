package io.greenstep.ui.places

import android.Manifest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.greenstep.data.economy.CoinStore
import io.greenstep.data.map.LatLng
import io.greenstep.data.map.haversineKm
import io.greenstep.data.streak.StreakStore
import io.greenstep.ui.components.ConstrainedText
import io.greenstep.ui.components.rememberHaptics
import io.greenstep.ui.theme.Green100
import io.greenstep.ui.theme.Green500
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ThemeManager
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GreenPlace(val id: String, val name: String, val type: String, val location: LatLng, val description: String)
data class LeaderEntry(val name: String, val visits: Int)

private fun curatedPlaces(): List<GreenPlace> = listOf(
    GreenPlace("p1","Central Meadow Park","Park", LatLng(40.7128,-74.0060),"Large urban park with oak trail"),
    GreenPlace("p2","Sunrise Community Garden","Garden", LatLng(40.7150,-74.0100),"Volunteer tended flower beds"),
    GreenPlace("p3","Riverbank Cleanup Spot","Cleanup spot", LatLng(40.7180,-74.0020),"Weekly river litter pick"),
    GreenPlace("p4","Filiz Grove","Park", LatLng(40.7100,-74.0080),"Shaded grove • 1.2km loop"),
    GreenPlace("p5","Herb Spiral Garden","Garden", LatLng(40.7135,-74.0035),"Aromatic herbs & bees"),
    GreenPlace("p6","Old Mill Green","Park", LatLng(40.7200,-74.0120),"Historic mill & meadow"),
)

private fun leaderboardFor(placeId: String): List<LeaderEntry> {
    val seed = placeId.hashCode()
    val rnd = Random(seed)
    val names = listOf("Ava","Liam","Maya","Noah","Zara","Leo","Sora","Mina")
    return names.shuffled(rnd).take(3).mapIndexed { i, n -> LeaderEntry(n, rnd.nextInt(12,80) - i*5) }.sortedByDescending { it.visits }
}

@Composable
fun PlacesScreen(userLocation: LatLng? = null) {
    val places = remember { curatedPlaces() }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var checkedIns by remember { mutableStateOf(setOf<String>()) }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(places, key = { it.id }) { place ->
            val isChecked = checkedIns.contains(place.id)
            val distance = userLocation?.let { haversineKm(it, place.location) }
            PlaceCard(place = place, distanceKm = distance, isChecked = isChecked, onCheckIn = {
                if (isChecked) return@PlaceCard
                checkedIns = checkedIns + place.id
                scope.launch {
                    try { CoinStore(ctx).addCoins(10) } catch (_: Exception) {}
                    try { StreakStore(ctx).incrementStreak() } catch (_: Exception) {}
                }
            })
        }
    }
}

@Composable
private fun PlaceCard(place: GreenPlace, distanceKm: Double?, isChecked: Boolean, onCheckIn: () -> Unit) {
    val haptic = rememberHaptics()
    val ctx = LocalContext.current
    val reduce by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = if (reduce) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label = "press")
    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().scale(scale), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Green100), contentAlignment = Alignment.Center) {
                    Icon(imageVector = when(place.type) { "Garden" -> Icons.Outlined.LocalFlorist; "Cleanup spot" -> Icons.Outlined.Eco; else -> Icons.Outlined.Park }, contentDescription = null, tint = Green500, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ConstrainedText(text = place.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                    ConstrainedText(text = place.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, softWrap = true, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        ConstrainedText(text = place.type, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f, fill=false))
                        if (distanceKm != null) { Spacer(Modifier.width(8.dp)); ConstrainedText(text = "%.1f km away".format(distanceKm), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f, fill=false)) }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    ConstrainedText(text = "Leaderboard", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                        leaderboardFor(place.id).forEachIndexed { idx, entry ->
                            Row(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(if(idx==0) Green500 else MaterialTheme.colorScheme.primary.copy(alpha=0.3f)), contentAlignment = Alignment.Center) { Text(text = "${idx+1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, maxLines=1) }
                                Spacer(Modifier.width(4.dp))
                                ConstrainedText(text = "${entry.name} ${entry.visits}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { haptic.tick(); if (!isChecked) haptic.success(); onCheckIn() }, enabled = !isChecked, interactionSource = interaction, shape = RoundedCornerShape(24.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    ConstrainedText(text = if(isChecked) "Checked in!" else "Check-in", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (isChecked) ConstrainedText(text = "+10 coins • +1 streak", style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth())
        }
    }
}
