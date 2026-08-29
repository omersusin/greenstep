package io.greenstep.ui.settings

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.GreenStepApplication
import io.greenstep.R
import io.greenstep.data.day.DaySettings
import io.greenstep.data.economy.CoinStore
import io.greenstep.data.streak.StreakStore
import io.greenstep.ui.components.rememberHaptics
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ShapeFamily
import io.greenstep.ui.theme.ThemeChoice
import io.greenstep.ui.theme.ThemeManager
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as? GreenStepApplication
    val scope = rememberCoroutineScope()
    val haptic = rememberHaptics()
    val themeChoice by ThemeManager.themeChoiceFlow(context).collectAsState(initial = ThemeChoice.AUTO)
    val shapeFamily by ThemeManager.shapeFlow(context).collectAsState(initial = ShapeFamily.Rounded)
    val hapticsEnabled by ThemeManager.hapticsEnabledFlow(context).collectAsState(initial = true)
    val units by ThemeManager.unitsFlow(context).collectAsState(initial = "km")
    val reduceMotion by ThemeManager.reduceMotionFlow(context).collectAsState(initial = false)
    val dailyGoal by ThemeManager.dailyGoalFlow(context).collectAsState(initial = 7500)
    val stepLength by ThemeManager.stepLengthFlow(context).collectAsState(initial = 72)
    val height by ThemeManager.heightFlow(context).collectAsState(initial = 182)
    val weight by ThemeManager.weightFlow(context).collectAsState(initial = 70)
    val pace by ThemeManager.paceFlow(context).collectAsState(initial = 1f)
    val scrollState = rememberScrollState()
    var showClear by remember { mutableStateOf(false) }
    val goalAnimated by animateFloatAsState(targetValue = dailyGoal.toFloat(), animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.expressiveSpring, label = "goalAnim")

    fun doHaptic() { haptic.tick() }

    suspend fun syncDaySettings(update: DaySettings.() -> DaySettings) {
        val db = app?.greenStepDatabase ?: return
        val today = LocalDate.now()
        val current = DaySettings(date = today, goal = dailyGoal, stepLengthCm = stepLength, heightCm = height, weightKg = weight, pace = pace)
        val new = current.update()
        try { db.dayDao().updateDaySettings(new) } catch (_: Exception) {}
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
        PressableCard {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.settings_section_appearance), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                Text(text = stringResource(R.string.settings_theme), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                val row1Scroll = rememberScrollState()
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().horizontalScroll(row1Scroll)) {
                    SegmentedButton(selected = themeChoice == ThemeChoice.AUTO, onClick = { doHaptic(); scope.launch { ThemeManager.setTheme(context, ThemeChoice.AUTO) } }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4), label = { Text(text = stringResource(R.string.settings_theme_auto), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                    SegmentedButton(selected = themeChoice == ThemeChoice.LIGHT, onClick = { doHaptic(); scope.launch { ThemeManager.setTheme(context, ThemeChoice.LIGHT) } }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4), label = { Text(text = stringResource(R.string.settings_theme_light), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                    SegmentedButton(selected = themeChoice == ThemeChoice.DARK, onClick = { doHaptic(); scope.launch { ThemeManager.setTheme(context, ThemeChoice.DARK) } }, shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4), label = { Text(text = stringResource(R.string.settings_theme_dark), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                    SegmentedButton(selected = themeChoice == ThemeChoice.MEADOW, onClick = { doHaptic(); scope.launch { ThemeManager.setTheme(context, ThemeChoice.MEADOW) } }, shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4), label = { Text(text = stringResource(R.string.settings_theme_meadow), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                }
                val row2Scroll = rememberScrollState()
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().horizontalScroll(row2Scroll)) {
                    SegmentedButton(selected = themeChoice == ThemeChoice.OCEAN, onClick = { doHaptic(); scope.launch { ThemeManager.setTheme(context, ThemeChoice.OCEAN) } }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3), label = { Text(text = stringResource(R.string.settings_theme_ocean), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                    SegmentedButton(selected = themeChoice == ThemeChoice.SUNSET, onClick = { doHaptic(); scope.launch { ThemeManager.setTheme(context, ThemeChoice.SUNSET) } }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3), label = { Text(text = stringResource(R.string.settings_theme_sunset), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                    SegmentedButton(selected = themeChoice == ThemeChoice.NIGHT, onClick = { doHaptic(); scope.launch { ThemeManager.setTheme(context, ThemeChoice.NIGHT) } }, shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3), label = { Text(text = stringResource(R.string.settings_theme_night), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                }
                Text(text = stringResource(R.string.settings_shape), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                val shapeScroll = rememberScrollState()
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().horizontalScroll(shapeScroll)) {
                    SegmentedButton(selected = shapeFamily == ShapeFamily.Rounded, onClick = { doHaptic(); scope.launch { ThemeManager.setShape(context, ShapeFamily.Rounded) } }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3), label = { Text(text = stringResource(R.string.theme_shape_rounded), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                    SegmentedButton(selected = shapeFamily == ShapeFamily.Squircle, onClick = { doHaptic(); scope.launch { ThemeManager.setShape(context, ShapeFamily.Squircle) } }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3), label = { Text(text = stringResource(R.string.theme_shape_squircle), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                    SegmentedButton(selected = shapeFamily == ShapeFamily.Cut, onClick = { doHaptic(); scope.launch { ThemeManager.setShape(context, ShapeFamily.Cut) } }, shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3), label = { Text(text = stringResource(R.string.theme_shape_cut), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                }
            }
        }
        PressableCard {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.settings_section_goals), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                Text(text = stringResource(R.string.settings_daily_goal), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                Text(text = stringResource(R.string.settings_daily_goal_value, goalAnimated.toInt()), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                var lastBucket by remember { mutableStateOf(dailyGoal / 500) }
                Slider(value = dailyGoal.toFloat(), onValueChange = { v ->
                    val bucket = v.toInt() / 500
                    if (bucket != lastBucket) { lastBucket = bucket; if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                    scope.launch { ThemeManager.setDailyGoal(context, v.toInt()); syncDaySettings { copy(goal = v.toInt()) } }
                }, valueRange = 1000f..20000f, steps = 37)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallNumberField(label = stringResource(R.string.settings_step_length), value = stepLength, onValue = { scope.launch { ThemeManager.setStepLength(context, it); syncDaySettings { copy(stepLengthCm = it) } } }, modifier = Modifier.weight(1f))
                    SmallNumberField(label = stringResource(R.string.settings_height), value = height, onValue = { scope.launch { ThemeManager.setHeight(context, it); syncDaySettings { copy(heightCm = it) } } }, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallNumberField(label = stringResource(R.string.settings_weight), value = weight, onValue = { scope.launch { ThemeManager.setWeight(context, it); syncDaySettings { copy(weightKg = it) } } }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = pace.toString(), onValueChange = { s -> s.toFloatOrNull()?.let { f -> scope.launch { ThemeManager.setPace(context, f); syncDaySettings { copy(pace = f) } } } }, label = { Text(text = stringResource(R.string.settings_pace), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        }
        PressableCard {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.settings_section_units), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                Text(text = stringResource(R.string.settings_units), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                val unitsScroll = rememberScrollState()
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().horizontalScroll(unitsScroll)) {
                    SegmentedButton(selected = units == "km", onClick = { doHaptic(); scope.launch { ThemeManager.setUnits(context, "km") } }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2), label = { Text(text = stringResource(R.string.settings_units_km), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                    SegmentedButton(selected = units == "mi", onClick = { doHaptic(); scope.launch { ThemeManager.setUnits(context, "mi") } }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2), label = { Text(text = stringResource(R.string.settings_units_mi), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.widthIn(max = 80.dp)) })
                }
            }
        }
        PressableCard {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.settings_section_accessibility), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.settings_haptics), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                        Text(text = stringResource(R.string.settings_haptics_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                    }
                    val hScale by animateFloatAsState(targetValue = if (hapticsEnabled) 1f else 0.92f, animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.expressiveSpring, label = "hapticScale")
                    Switch(checked = hapticsEnabled, onCheckedChange = { checked -> doHaptic(); scope.launch { ThemeManager.setHapticsEnabled(context, checked) } }, modifier = Modifier.scale(hScale))
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.settings_reduce_motion), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                        Text(text = stringResource(R.string.settings_reduce_motion_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth().widthIn(max = 160.dp))
                    }
                    val rScale by animateFloatAsState(targetValue = if (reduceMotion) 1f else 0.92f, animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.expressiveSpring, label = "reduceScale")
                    Switch(checked = reduceMotion, onCheckedChange = { checked -> haptic.tick(); scope.launch { ThemeManager.setReduceMotion(context, checked) } }, modifier = Modifier.scale(rScale))
                }
            }
        }
        PressableCard {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.settings_section_data), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                Text(text = stringResource(R.string.settings_export_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    val btnInter = remember { MutableInteractionSource() }
                    val pressed by btnInter.collectIsPressedAsState()
                    val btnScale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label = "exportScale")
                    Button(onClick = {
                        doHaptic()
                        scope.launch {
                            try {
                                val flow = app?.greenStepDatabase?.dayDao()?.getAllDays()
                                val days = flow?.first() ?: emptyList()
                                val csv = buildString {
                                    appendLine("date,steps,goal,distanceKm,calories")
                                    days.forEach { d -> appendLine("${d.date},${d.steps},${d.goal},${d.distanceKm},${d.calories}") }
                                }
                                val file = java.io.File(context.cacheDir, "greenstep_export.csv")
                                file.writeText(csv)
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Export CSV"))
                            } catch (e: Exception) {
                                Toast.makeText(context, context.getString(R.string.settings_export_done), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, interactionSource = btnInter, modifier = Modifier.scale(btnScale).weight(1f)) {
                        Text(text = stringResource(R.string.settings_export_csv), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                    }
                    val clearInter = remember { MutableInteractionSource() }
                    val clearPressed by clearInter.collectIsPressedAsState()
                    val clearScale by animateFloatAsState(targetValue = if (clearPressed) 0.97f else 1f, animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label = "clearScale")
                    OutlinedButton(onClick = { doHaptic(); showClear = true }, interactionSource = clearInter, modifier = Modifier.scale(clearScale).weight(1f)) {
                        Text(text = stringResource(R.string.settings_clear_data), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                    }
                }
                Text(text = stringResource(R.string.settings_clear_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        PressableCard {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.settings_section_about), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                Image(painter = painterResource(R.drawable.filiz_sleeping), contentDescription = stringResource(R.string.filiz_dialog_title), modifier = Modifier.size(96.dp))
                Text(text = stringResource(R.string.settings_about_filiz), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(text = stringResource(R.string.settings_about_version), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(text = stringResource(R.string.data_consent), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    if (showClear) {
        AlertDialog(onDismissRequest = { showClear = false }, title = { Text(text = stringResource(R.string.settings_clear_confirm), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) }, text = { Text(text = stringResource(R.string.settings_clear_desc)) }, confirmButton = { TextButton(onClick = {
            showClear = false
            scope.launch {
                try { app?.greenStepDatabase?.clearAllTables() } catch (_: Exception) {}
                try { ThemeManager.clearAll(context) } catch (_: Exception) {}
                try { CoinStore(context).clear() } catch (_: Exception) {}
                try { StreakStore(context).clear() } catch (_: Exception) {}
                try { context.cacheDir.listFiles()?.forEach { if (it.name.startsWith("greenstep_export")) it.delete() } } catch (_: Exception) {}
                haptic.success()
                Toast.makeText(context, context.getString(R.string.settings_cleared), Toast.LENGTH_SHORT).show()
            }
        }) { Text(text = stringResource(R.string.settings_clear_yes)) } }, dismissButton = { TextButton(onClick = { showClear = false }) { Text(text = stringResource(R.string.settings_clear_no)) } })
    }
}

@Composable
private fun SmallNumberField(label: String, value: Int, onValue: (Int) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(value = text, onValueChange = { s -> text = s; s.toIntOrNull()?.let(onValue) }, label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = modifier)
}

@Composable
private fun PressableCard(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val reduceMotion by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label = "cardPress")
    val haptic = rememberHaptics()
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().scale(scale), onClick = { haptic.tick() }, interactionSource = interaction) { content() }
}
