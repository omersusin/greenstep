package io.greenstep.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.greenstep.R
import io.greenstep.data.map.LatLng
import io.greenstep.service.GpsTrackingService
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private fun haversineKm(a: LatLng, b: LatLng): Double {
    val r = 6371.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val s1 = sin(dLat / 2)
    val s2 = sin(dLng / 2)
    val h = s1 * s1 + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * s2 * s2
    return 2 * r * atan2(sqrt(h), sqrt(1 - h))
}

private fun totalDistanceKm(points: List<LatLng>): Double {
    if (points.size < 2) return 0.0
    var sum = 0.0
    for (i in 1 until points.size) sum += haversineKm(points[i - 1], points[i])
    return sum
}

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }
    val points = remember { mutableStateListOf<LatLng>() }
    var isTracking by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var distanceKm by remember { mutableStateOf(0.0) }

    DisposableEffect(isTracking, isPaused, hasPermission) {
        if (!isTracking || isPaused || !hasPermission) { onDispose {} ; return@DisposableEffect onDispose {} }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = LocationListener { loc: Location ->
            val p = LatLng(loc.latitude, loc.longitude)
            points.add(p)
            distanceKm = totalDistanceKm(points)
        }
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, listener)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 5f, listener)
        } catch (_: Exception) { }
        onDispose { try { lm.removeUpdates(listener) } catch (_: Exception) {} }
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // OSM placeholder (e.g. osmdroid MapView). Google Maps fallback:
                // Replace AndroidView below with com.google.android.gms.maps.MapView when
                // com.google.android.gms:play-services-maps is added.
                AndroidView(
                    factory = { ctx ->
                        android.view.View(ctx).apply {
                            setBackgroundColor(android.graphics.Color.parseColor("#C8E6C9"))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "OSM Map Placeholder", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.map_osm_attribution),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (points.isNotEmpty()) {
                        Text(
                            text = "${points.size} pts • ${"%.2f".format(distanceKm)} km",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (!hasPermission) {
                        OutlinedButton(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }, modifier = Modifier.padding(top = 8.dp)) {
                            Text(stringResource(R.string.map_grant_permission))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isTracking) {
                    Button(
                        onClick = {
                            if (!hasPermission) { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION); return@Button }
                            points.clear(); distanceKm = 0.0; isTracking = true; isPaused = false
                            GpsTrackingHelper.start(context)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.map_start)) }
                } else {
                    OutlinedButton(
                        onClick = { isPaused = !isPaused; if (isPaused) GpsTrackingHelper.pause(context) else GpsTrackingHelper.resume(context) },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(if (isPaused) R.string.map_resume else R.string.map_pause)) }
                    Button(
                        onClick = { isTracking = false; isPaused = false; GpsTrackingHelper.stop(context) },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.map_stop)) }
                }
            }
            Text(
                text = stringResource(R.string.map_distance, "%.2f".format(distanceKm)),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)
            )
        }
    }
}

private object GpsTrackingHelper {
    fun start(ctx: Context) = startService(ctx, GpsTrackingService.ACTION_START)
    fun pause(ctx: Context) = startService(ctx, GpsTrackingService.ACTION_PAUSE)
    fun resume(ctx: Context) = startService(ctx, GpsTrackingService.ACTION_RESUME)
    fun stop(ctx: Context) = startService(ctx, GpsTrackingService.ACTION_STOP)
    private fun startService(ctx: Context, action: String) {
        val intent = android.content.Intent(ctx, GpsTrackingService::class.java).apply { this.action = action }
        try { ContextCompat.startForegroundService(ctx, intent) } catch (_: Exception) { ctx.startService(intent) }
    }
}
