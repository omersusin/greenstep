package io.greenstep.ui.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.R
import io.greenstep.data.economy.CoinStore
import io.greenstep.ui.theme.GreenStepMotion
import kotlinx.coroutines.launch

private data class ShopItem(
    val id: String,
    val titleRes: Int,
    val price: Int,
    val icon: ImageVector,
    val descRes: Int
)

private val shopItems = listOf(
    ShopItem("freeze", R.string.shop_item_freeze, 200, Icons.Outlined.AcUnit, R.string.shop_item_freeze_desc),
    ShopItem("mystery_seed", R.string.shop_item_mystery_seed, 300, Icons.Outlined.Eco, R.string.shop_item_mystery_seed_desc),
    ShopItem("skin_sprout", R.string.shop_item_skin_sprout, 150, Icons.Outlined.Spa, R.string.shop_item_skin_sprout_desc),
    ShopItem("skin_forest", R.string.shop_item_skin_forest, 400, Icons.Outlined.Palette, R.string.shop_item_skin_forest_desc),
    ShopItem("skin_golden", R.string.shop_item_skin_golden, 500, Icons.Outlined.Palette, R.string.shop_item_skin_golden_desc),
    ShopItem("skin_ocean", R.string.shop_item_skin_ocean, 350, Icons.Outlined.Palette, R.string.shop_item_skin_ocean_desc),
)

@Composable
fun ShopScreen(
    coinStore: CoinStore? = null
) {
    val context = LocalContext.current
    val store = remember { coinStore ?: CoinStore(context) }
    val balance by store.balanceFlow.collectAsState(initial = 0)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notEnough = stringResource(R.string.shop_not_enough)
    val purchased = stringResource(R.string.shop_purchased)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.shop_title),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        text = stringResource(R.string.shop_balance, balance),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(shopItems, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(item.titleRes),
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = stringResource(item.descRes),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = stringResource(R.string.shop_price, item.price),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            val btnInteraction = remember { MutableInteractionSource() }
                            val pressed by btnInteraction.collectIsPressedAsState()
                            val btnScale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = GreenStepMotion.pressSpring, label = "buyScale")
                            val haptic = LocalHapticFeedback.current
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch {
                                        val ok = store.spendCoins(item.price)
                                        snackbarHostState.showSnackbar(if (ok) purchased else notEnough)
                                    }
                                },
                                interactionSource = btnInteraction,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).scale(btnScale)
                            ) {
                                Text(
                                    text = stringResource(R.string.shop_buy),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
