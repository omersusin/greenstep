package io.greenstep.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.greenstep.R
import io.greenstep.ui.components.rememberHaptics
import io.greenstep.ui.theme.Green500
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ThemeManager

@Composable
fun ActivityScreen() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    var isRunning by remember { mutableStateOf(false) }
    ActivityContent(
        hasPermission = hasPermission,
        isRunning = isRunning,
        onRequestPermission = {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        },
        onToggleRun = { isRunning = !isRunning },
        distanceKm = 0.0,
        pace = "--:--",
        calories = 0,
    )
}

@Composable
internal fun ActivityContent(
    hasPermission: Boolean,
    isRunning: Boolean,
    onRequestPermission: () -> Unit,
    onToggleRun: () -> Unit,
    distanceKm: Double,
    pace: String,
    calories: Int,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!hasPermission) {
            item(key = "rationale") {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.activity_permission_title),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(R.string.activity_permission_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val grantHaptics = rememberHaptics()
                        val grantCtx = LocalContext.current
                        val grantReduce by ThemeManager.reduceMotionFlow(grantCtx).collectAsState(initial = false)
                        val grantInter = remember { MutableInteractionSource() }
                        val grantPressed by grantInter.collectIsPressedAsState()
                        val grantScale by animateFloatAsState(targetValue = if (grantPressed) 0.97f else 1f, animationSpec = if (grantReduce) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label = "grantScale")
                        TextButton(onClick = { grantHaptics.tick(); onRequestPermission() }, interactionSource = grantInter, modifier = Modifier.scale(grantScale).widthIn(max = 200.dp)) {
                            Text(
                                text = stringResource(R.string.activity_permission_grant),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
        item(key = "map") {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                val ctx = LocalContext.current
                val reduceMotion by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
                Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width; val h = size.height; val grid = Color(0x1A000000)
                        for (i in 1..3) { drawLine(grid, Offset(0f, h * i / 4f), Offset(w, h * i / 4f), strokeWidth = 1f); drawLine(grid, Offset(w * i / 4f, 0f), Offset(w * i / 4f, h), strokeWidth = 1f) }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!reduceMotion) {
                                val inf = rememberInfiniteTransition(label = "pulse")
                                val pulse by inf.animateFloat(initialValue = 0.9f, targetValue = 1.25f, animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse")
                                Box(modifier = Modifier.size(72.dp).scale(pulse).clip(CircleShape).background(Green500.copy(alpha = 0.18f)))
                            }
                            Image(painter = painterResource(R.drawable.filiz_sprout), contentDescription = null, modifier = Modifier.size(64.dp))
                            Box(modifier = Modifier.align(Alignment.TopEnd).size(14.dp).clip(CircleShape).background(Green500))
                            if (!reduceMotion) {
                                val inf2 = rememberInfiniteTransition(label = "dot")
                                val s by inf2.animateFloat(initialValue = 0.8f, targetValue = 1.15f, animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "dot")
                                Box(modifier = Modifier.align(Alignment.TopEnd).size(10.dp).scale(s).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)))
                            }
                        }
                        Text(text = "Filiz is exploring 🌱", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 220.dp))
                        Text(text = stringResource(R.string.activity_map_placeholder), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 12.dp).widthIn(max = 260.dp))
                    }
                }
            }
        }
        item(key = "stats") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = stringResource(R.string.activity_stat_distance), value = stringResource(R.string.activity_stat_distance_value, distanceKm), modifier = Modifier.weight(1f))
                StatCard(title = stringResource(R.string.activity_stat_pace), value = stringResource(R.string.activity_stat_pace_value, pace), modifier = Modifier.weight(1f))
                StatCard(title = stringResource(R.string.activity_stat_calories), value = stringResource(R.string.activity_stat_calories_value, calories), modifier = Modifier.weight(1f))
            }
        }
        item(key = "button") {
            val haptics = rememberHaptics()
            val ctx2 = LocalContext.current
            val reduce by ThemeManager.reduceMotionFlow(ctx2).collectAsState(initial = false)
            val interaction = remember { MutableInteractionSource() }
            val pressed by interaction.collectIsPressedAsState()
            val spec = if (reduce) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring
            val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = spec, label = "startScale")
            Button(
                onClick = { haptics.tick(); onToggleRun(); if (!isRunning) haptics.success() },
                interactionSource = interaction,
                modifier = Modifier.fillMaxWidth().scale(scale).widthIn(max = 400.dp),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(16.dp),
            ) {
                Icon(imageVector = if (isRunning) Icons.Outlined.Stop else Icons.Outlined.PlayArrow, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(if (isRunning) R.string.activity_stop else R.string.activity_start),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
            )
        }
    }
}
