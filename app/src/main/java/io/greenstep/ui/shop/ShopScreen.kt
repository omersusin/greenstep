package io.greenstep.ui.shop

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.R
import io.greenstep.data.economy.CoinStore
import io.greenstep.ui.components.rememberHaptics
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class Rarity(val label: String, val color: Color) {
    COMMON("Common", Color(0xFF9E9E9E)),
    RARE("Rare", Color(0xFF42A5F5)),
    EPIC("Epic", Color(0xFFAB47BC)),
    LEGENDARY("Legendary", Color(0xFFFFB300))
}

private data class ShopItem(
    val id: String,
    val titleRes: Int,
    val price: Int,
    val icon: ImageVector,
    val descRes: Int,
    val rarity: Rarity
)

private val shopItems = listOf(
    ShopItem("freeze", R.string.shop_item_freeze, 200, Icons.Outlined.AcUnit, R.string.shop_item_freeze_desc, Rarity.RARE),
    ShopItem("mystery_seed", R.string.shop_item_mystery_seed, 300, Icons.Outlined.CardGiftcard, R.string.shop_item_mystery_seed_desc, Rarity.EPIC),
    ShopItem("skin_sprout", R.string.shop_item_skin_sprout, 150, Icons.Outlined.Spa, R.string.shop_item_skin_sprout_desc, Rarity.COMMON),
    ShopItem("skin_forest", R.string.shop_item_skin_forest, 400, Icons.Outlined.Palette, R.string.shop_item_skin_forest_desc, Rarity.RARE),
    ShopItem("skin_golden", R.string.shop_item_skin_golden, 500, Icons.Outlined.Palette, R.string.shop_item_skin_golden_desc, Rarity.LEGENDARY),
    ShopItem("skin_ocean", R.string.shop_item_skin_ocean, 350, Icons.Outlined.Palette, R.string.shop_item_skin_ocean_desc, Rarity.EPIC),
    ShopItem("loot_box", R.string.shop_item_mystery_seed, 250, Icons.Outlined.Stars, R.string.shop_item_mystery_seed_desc, Rarity.EPIC)
)

private fun rollRarity(): Rarity {
    val r = Random.nextFloat()
    return when {
        r < 0.55f -> Rarity.COMMON
        r < 0.80f -> Rarity.RARE
        r < 0.95f -> Rarity.EPIC
        else -> Rarity.LEGENDARY
    }
}

private fun bonusForRarity(r: Rarity): Int = when (r) {
    Rarity.COMMON -> Random.nextInt(10, 30)
    Rarity.RARE -> Random.nextInt(30, 70)
    Rarity.EPIC -> Random.nextInt(70, 150)
    Rarity.LEGENDARY -> Random.nextInt(150, 350)
}

@Composable
fun ShopScreen(coinStore: CoinStore? = null) {
    val context = LocalContext.current
    val store = remember { coinStore ?: CoinStore(context) }
    val balance by store.balanceFlow.collectAsState(initial = 0)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notEnough = stringResource(R.string.shop_not_enough)
    val purchased = stringResource(R.string.shop_purchased)
    var owned by remember { mutableStateOf(setOf<String>()) }
    var lastLoot by remember { mutableStateOf<Pair<Rarity, Int>?>(null) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.shop_title), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.weight(1f))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), modifier = Modifier.widthIn(max = 160.dp)) {
                    Text(text = stringResource(R.string.shop_balance, balance), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).widthIn(max = 120.dp), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            if (lastLoot != null) {
                val reduceMotion by ThemeManager.reduceMotionFlow(LocalContext.current).collectAsState(initial = false)
                var sparkle by remember(lastLoot) { mutableStateOf(true) }
                LaunchedEffect(lastLoot) { sparkle = true; delay(700); sparkle = false }
                val lootScale by animateFloatAsState(targetValue = if (sparkle && !reduceMotion) 1.02f else 1f, animationSpec = GreenStepMotion.expressiveSpringSpec(), label = "lootSparkle")
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).scale(lootScale), colors = CardDefaults.cardColors(containerColor = lastLoot!!.first.color.copy(alpha = 0.15f)), shape = RoundedCornerShape(14.dp)) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (sparkle) "✨🎲✨" else "🎲", style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(max = 64.dp))
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(text = "Loot: ${lastLoot!!.first.label} +${lastLoot!!.second} coins!", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 260.dp))
                            Text(text = "Variable reward — Filiz is generous 🌱", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.widthIn(max = 260.dp))
                        }
                    }
                }
            }
            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(shopItems, key = { it.id }) { item ->
                    val isOwned = owned.contains(item.id) && item.id != "mystery_seed" && item.id != "loot_box"
                    val canAfford = balance >= item.price
                    val enabled = canAfford && !isOwned
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isOwned) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(imageVector = item.icon, contentDescription = null, tint = if (isOwned) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp).align(Alignment.Center))
                            }
                            AssistChip(onClick = {}, label = { Text(item.rarity.label, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) }, colors = AssistChipDefaults.assistChipColors(containerColor = item.rarity.color.copy(alpha = 0.18f), labelColor = item.rarity.color), modifier = Modifier.padding(top = 6.dp).widthIn(max = 120.dp))
                            Text(text = stringResource(item.titleRes).let { if (item.id == "loot_box") "Mystery Loot Box" else it }, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.padding(top = 6.dp).fillMaxWidth())
                            Text(text = stringResource(item.descRes).let { if (item.id == "loot_box") "Variable reward —rarity roll!" else it }, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp).fillMaxWidth())
                            Text(text = stringResource(R.string.shop_price, item.price), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 6.dp))
                            if (isOwned) Text(text = "Owned ✓", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                            val reduceMotion by ThemeManager.reduceMotionFlow(LocalContext.current).collectAsState(initial = false)
                            val spec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring
                            val btnInteraction = remember { MutableInteractionSource() }
                            val pressed by btnInteraction.collectIsPressedAsState()
                            val btnScale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = spec, label = "buyScale")
                            val haptic = rememberHaptics()
                            Button(
                                onClick = {
                                    if (!canAfford) {
                                        haptic.error()
                                        scope.launch { snackbarHostState.showSnackbar(notEnough) }
                                        return@Button
                                    }
                                    if (isOwned) return@Button
                                    haptic.tick()
                                    scope.launch {
                                        val ok = store.spendCoins(item.price)
                                        if (!ok) {
                                            haptic.error()
                                            snackbarHostState.showSnackbar(notEnough)
                                        } else {
                                            if (item.id == "mystery_seed" || item.id == "loot_box") {
                                                val rarity = rollRarity()
                                                val bonus = bonusForRarity(rarity)
                                                store.addCoins(bonus)
                                                lastLoot = rarity to bonus
                                                snackbarHostState.showSnackbar("🎁 ${rarity.label} reward +$bonus coins!")
                                                haptic.success()
                                            } else {
                                                owned = owned + item.id
                                                snackbarHostState.showSnackbar(purchased)
                                                haptic.success()
                                            }
                                        }
                                    }
                                },
                                interactionSource = btnInteraction,
                                enabled = enabled || (!canAfford),
                                colors = if (!enabled && !canAfford) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant) else if (isOwned) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else ButtonDefaults.buttonColors(),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).scale(btnScale).widthIn(max = 200.dp)
                            ) {
                                Text(text = when { isOwned -> "Owned"; !canAfford -> "Need coins"; item.id == "loot_box" || item.id == "mystery_seed" -> "Open 🎲"; else -> stringResource(R.string.shop_buy) }, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 140.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun vibrateError(context: android.content.Context) {
    try {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION") context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vib?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 40, 80), -1))
            else @Suppress("DEPRECATION") vib.vibrate(longArrayOf(0, 80, 40, 80), -1)
        }
    } catch (_: Exception) {}
}
