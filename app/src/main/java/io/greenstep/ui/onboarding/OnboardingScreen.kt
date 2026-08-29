package io.greenstep.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.greenstep.R
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ThemeManager
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reduceMotion by ThemeManager.reduceMotionFlow(context).collectAsState(initial = false)
    val pagerState = rememberPagerState(initialPage = 0) { 3 }
    val animSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.expressiveSpring

    val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun complete() {
        scope.launch {
            ThemeManager.setOnboardingCompleted(context, true)
            onFinished()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { complete() }) {
                Text(text = stringResource(R.string.onboarding_skip), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            beyondViewportPageCount = 1
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (page) {
                    0 -> OnboardPage(
                        drawable = R.drawable.filiz_seed,
                        title = stringResource(R.string.onboarding_page1_title),
                        body = stringResource(R.string.onboarding_page1_body),
                        reduceMotion = reduceMotion
                    )
                    1 -> {
                        OnboardPage(
                            drawable = R.drawable.filiz_sprout,
                            title = stringResource(R.string.onboarding_page2_title),
                            body = stringResource(R.string.onboarding_page2_body),
                            reduceMotion = reduceMotion
                        )
                        Spacer(Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = stringResource(R.string.onboarding_perm_activity_title), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                                Text(text = stringResource(R.string.data_consent), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val hasAct = ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
                                if (!hasAct) {
                                    Button(onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) activityLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                    }, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = stringResource(R.string.onboarding_perm_activity_grant), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                                    }
                                } else {
                                    Text(text = "✓ Granted", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    2 -> {
                        OnboardPage(
                            drawable = R.drawable.filiz_tree,
                            title = stringResource(R.string.onboarding_page3_title),
                            body = stringResource(R.string.onboarding_page3_body),
                            reduceMotion = reduceMotion
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(text = stringResource(R.string.data_consent), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
                        Spacer(Modifier.height(12.dp))
                        if (Build.VERSION.SDK_INT >= 33) {
                            val hasNotif = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                            if (!hasNotif) {
                                Button(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = stringResource(R.string.onboarding_perm_notif_grant), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                                }
                            } else {
                                Text(text = "✓ Notifications enabled", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { i ->
                val selected = pagerState.currentPage == i
                val scale by animateFloatAsState(targetValue = if (selected) 1.35f else 1f, animationSpec = animSpec, label = "dotScale")
                Box(modifier = Modifier.padding(4.dp).size(8.dp).scale(scale).clip(CircleShape).background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            val isLast = pagerState.currentPage == 2
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (isLast) complete()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(if (isLast) R.string.onboarding_done else R.string.onboarding_next), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
            }
        }
    }
}

@Composable
private fun OnboardPage(drawable: Int, title: String, body: String, reduceMotion: Boolean) {
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = if (reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.expressiveSpringSpec(), label = "pageScale")
    Image(painter = painterResource(drawable), contentDescription = title, modifier = Modifier.size(160.dp).scale(scale))
    Spacer(Modifier.height(20.dp))
    Text(text = title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    Spacer(Modifier.height(8.dp))
    Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 4, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 16.dp))
}
