package io.greenstep.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.greenstep.R
import io.greenstep.ui.theme.ThemeChoice
import io.greenstep.ui.theme.ThemeManager
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeChoice by ThemeManager.themeChoiceFlow(context).collectAsState(initial = ThemeChoice.AUTO)
    val hapticsEnabled by ThemeManager.hapticsEnabledFlow(context).collectAsState(initial = true)
    val units by ThemeManager.unitsFlow(context).collectAsState(initial = "km")
    val reduceMotion by ThemeManager.reduceMotionFlow(context).collectAsState(initial = false)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            modifier = Modifier.fillMaxWidth()
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_section_appearance),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = themeChoice == ThemeChoice.LIGHT,
                        onClick = { scope.launch { ThemeManager.setTheme(context, ThemeChoice.LIGHT) } },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 5),
                        label = {
                            Text(
                                text = stringResource(R.string.settings_theme_light),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                    )
                    SegmentedButton(
                        selected = themeChoice == ThemeChoice.DARK,
                        onClick = { scope.launch { ThemeManager.setTheme(context, ThemeChoice.DARK) } },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 5),
                        label = {
                            Text(
                                text = stringResource(R.string.settings_theme_dark),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                    )
                    SegmentedButton(
                        selected = themeChoice == ThemeChoice.MEADOW,
                        onClick = { scope.launch { ThemeManager.setTheme(context, ThemeChoice.MEADOW) } },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 5),
                        label = {
                            Text(
                                text = stringResource(R.string.settings_theme_meadow),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                    )
                    SegmentedButton(
                        selected = themeChoice == ThemeChoice.NIGHT,
                        onClick = { scope.launch { ThemeManager.setTheme(context, ThemeChoice.NIGHT) } },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 5),
                        label = {
                            Text(
                                text = stringResource(R.string.settings_theme_night),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                    )
                    SegmentedButton(
                        selected = themeChoice == ThemeChoice.AUTO,
                        onClick = { scope.launch { ThemeManager.setTheme(context, ThemeChoice.AUTO) } },
                        shape = SegmentedButtonDefaults.itemShape(index = 4, count = 5),
                        label = {
                            Text(
                                text = stringResource(R.string.settings_theme_auto),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                    )
                }
            }
        }
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_section_units),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.settings_units),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = units == "km",
                        onClick = { scope.launch { ThemeManager.setUnits(context, "km") } },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = {
                            Text(
                                text = stringResource(R.string.settings_units_km),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                    )
                    SegmentedButton(
                        selected = units == "mi",
                        onClick = { scope.launch { ThemeManager.setUnits(context, "mi") } },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = {
                            Text(
                                text = stringResource(R.string.settings_units_mi),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                    )
                }
            }
        }
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_section_accessibility),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_haptics),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.settings_haptics_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { checked -> scope.launch { ThemeManager.setHapticsEnabled(context, checked) } }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_reduce_motion),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.settings_reduce_motion_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Switch(
                        checked = reduceMotion,
                        onCheckedChange = { checked -> scope.launch { ThemeManager.setReduceMotion(context, checked) } }
                    )
                }
            }
        }
    }
}
