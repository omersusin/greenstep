package io.greenstep.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.greenstep.R
import io.greenstep.data.map.GpxExporter
import io.greenstep.data.map.LatLng
import io.greenstep.data.map.RouteDatabase
import io.greenstep.data.map.formatDuration
import io.greenstep.data.map.formatPace
import io.greenstep.data.map.totalDistanceKm
import io.greenstep.service.GpsTrackingService
import io.greenstep.ui.components.ConstrainedText
import io.greenstep.ui.components.rememberHaptics
import io.greenstep.ui.places.PlacesScreen
import io.greenstep.ui.theme.Green500
import io.greenstep.ui.theme.GreenStepMotion
import io.greenstep.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            clipToOutline = true
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            maxZoomLevel = 19.0
            minZoomLevel = 4.0
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(40.7128, -74.0060))
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val observer = remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
    }
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return mapView
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val haptic = rememberHaptics()
    val reduceMotion by ThemeManager.reduceMotionFlow(context).collectAsState(initial = false)
    var selectedTab by remember { mutableStateOf(0) }
    var hasFine by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    var hasCoarse by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    var hasBg by remember { mutableStateOf(if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED else true) }
    var hasNotif by remember { mutableStateOf(if(Build.VERSION.SDK_INT>=33) ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED else true) }
    var hasActivity by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)==PackageManager.PERMISSION_GRANTED) }
    val fineLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ hasFine=it; if(it) hasCoarse=true }
    val multiLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ m -> hasFine=m[Manifest.permission.ACCESS_FINE_LOCATION]==true; hasCoarse=m[Manifest.permission.ACCESS_COARSE_LOCATION]==true }
    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ hasBg=it }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ hasNotif=it }
    val actLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ hasActivity=it }
    val hasPermission = hasFine || hasCoarse
    val servicePoints by GpsTrackingService.points.collectAsState()
    val serviceDistance by GpsTrackingService.distance.collectAsState()
    val serviceDuration by GpsTrackingService.duration.collectAsState()
    val serviceTracking by GpsTrackingService.isTracking.collectAsState()
    val servicePaused by GpsTrackingService.isPaused.collectAsState()
    val localPoints = remember { mutableStateListOf<LatLng>() }
    var localDistance by remember { mutableStateOf(0.0) }
    var startMs by remember { mutableStateOf(0L) }
    var elapsed by remember { mutableStateOf(0L) }
    var confettiTick by remember { mutableStateOf(0) }
    val points = if(serviceTracking || servicePoints.isNotEmpty()) servicePoints else localPoints.toList()
    val distanceKm = if(serviceTracking || servicePoints.isNotEmpty()) serviceDistance else localDistance
    val durationMs = if(serviceTracking || servicePoints.isNotEmpty()) serviceDuration else elapsed
    val pace = formatPace(durationMs, distanceKm)
    val isTracking = serviceTracking
    val isPaused = servicePaused
    LaunchedEffect(isTracking, isPaused) { if(isTracking && !isPaused){ startMs=System.currentTimeMillis()-durationMs; while(true){ delay(1000); if(!isPaused) elapsed=System.currentTimeMillis()-startMs } } }
    DisposableEffect(hasPermission, isTracking, isPaused) {
        if(isTracking || isPaused || hasPermission || serviceTracking) { onDispose{}; return@DisposableEffect onDispose{} }
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        val listener = LocationListener { loc: Location -> val p=LatLng(loc.latitude, loc.longitude); localPoints.add(p); localDistance=totalDistanceKm(localPoints) }
        try{ lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000L,2f,listener); lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3000L,5f,listener)}catch(_:Exception){}
        onDispose{ try{lm.removeUpdates(listener)}catch(_:Exception){} }
    }
    fun ensureNotifAndStart(action:String){
        if(Build.VERSION.SDK_INT>=33 && !hasNotif){ notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS); return }
        val intent = Intent(context, GpsTrackingService::class.java).apply{ this.action=action }
        try{ ContextCompat.startForegroundService(context,intent)}catch(_:Exception){ context.startService(intent)}
    }
    val mapView = rememberMapViewWithLifecycle()
    LaunchedEffect(points, isTracking) {
        if(points.isEmpty()) return@LaunchedEffect
        val last = points.last()
        val gp = GeoPoint(last.latitude, last.longitude)
        mapView.controller.animateTo(gp)
        if(points.size==1) mapView.controller.setZoom(16.0)
    }
    Column(modifier = Modifier.fillMaxSize()){
        TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface){
            Tab(selected = selectedTab==0, onClick = {selectedTab=0; haptic.tick()}, text={ConstrainedText(text=stringResource(R.string.map_tab_map), style=MaterialTheme.typography.labelLarge)})
            Tab(selected = selectedTab==1, onClick = {selectedTab=1; haptic.tick()}, text={ConstrainedText(text=stringResource(R.string.map_tab_places), style=MaterialTheme.typography.labelLarge)})
        }
        if(selectedTab==1){ PlacesScreen(userLocation = points.lastOrNull()) ; return@Column}
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)){
            if(!hasPermission){ item{ PermissionCard(title=stringResource(R.string.activity_permission_title), body=stringResource(R.string.activity_permission_body), action=stringResource(R.string.activity_permission_grant), onClick={ multiLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))})}}
            if(hasPermission && !hasBg && Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){ item{ PermissionCard(title=stringResource(R.string.map_permission_bg_title), body=stringResource(R.string.map_permission_bg_body), action=stringResource(R.string.map_permission_bg_grant), onClick={ bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)})}}
            if(!hasNotif && Build.VERSION.SDK_INT>=33){ item{ PermissionCard(title=stringResource(R.string.map_permission_notif_title), body=stringResource(R.string.map_permission_notif_body), action=stringResource(R.string.activity_permission_grant), onClick={ notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)})}}
            if(!hasActivity){ item{ PermissionCard(title=stringResource(R.string.map_permission_act_title), body=stringResource(R.string.map_permission_act_body), action=stringResource(R.string.activity_permission_grant), onClick={ actLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)})}}
            item{
                Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)){
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center){
                        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize(), update = { mv ->
                            mv.overlays.removeAll { it is Polyline || it is Marker }
                            if(points.isNotEmpty()){
                                val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }
                                val polyline = Polyline().apply {
                                    setPoints(geoPoints)
                                    outlinePaint.color = android.graphics.Color.parseColor("#2E7D32")
                                    outlinePaint.strokeWidth = 10f
                                }
                                mv.overlays.add(polyline)
                                val whiteLine = Polyline().apply {
                                    setPoints(geoPoints)
                                    outlinePaint.color = android.graphics.Color.WHITE
                                    outlinePaint.strokeWidth = 3f
                                }
                                mv.overlays.add(whiteLine)
                                val start = Marker(mv).apply {
                                    position = geoPoints.first()
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                    icon = ContextCompat.getDrawable(context, R.drawable.filiz_sprout)?.apply { setBounds(0,0,48,48) }
                                    title = "Start"
                                }
                                mv.overlays.add(start)
                                if(geoPoints.size>1){
                                    val end = Marker(mv).apply {
                                        position = geoPoints.last()
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                        icon = ContextCompat.getDrawable(context, R.drawable.filiz_tree)?.apply { setBounds(0,0,48,48) }
                                        title = "Now"
                                    }
                                    mv.overlays.add(end)
                                }
                                mv.invalidate()
                            } else {
                                mv.overlays.clear()
                                mv.invalidate()
                            }
                        })
                        if(points.isEmpty()){
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)){
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp)) {
                                    if (!reduceMotion) {
                                        val inf = rememberInfiniteTransition(label="mapPulse")
                                        val s by inf.animateFloat(initialValue=0.85f, targetValue=1.2f, animationSpec=infiniteRepeatable(tween(900), RepeatMode.Reverse), label="s")
                                        Box(modifier = Modifier.size(72.dp).scale(s).clip(CircleShape).background(Green500.copy(alpha=0.16f)))
                                    }
                                    Image(painter = painterResource(R.drawable.filiz_sprout), contentDescription = null, modifier = Modifier.size(64.dp))
                                    Box(modifier = Modifier.align(Alignment.TopEnd).size(12.dp).clip(CircleShape).background(Color.White))
                                    Box(modifier = Modifier.align(Alignment.TopEnd).size(8.dp).clip(CircleShape).background(Green500))
                                }
                                Text(text = "Filiz is ready 🌱", style = MaterialTheme.typography.titleMedium, maxLines=1, overflow=TextOverflow.Ellipsis, softWrap=false, modifier=Modifier.widthIn(max=200.dp))
                                ConstrainedText(text = stringResource(R.string.map_osm_attribution), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top=4.dp).widthIn(max=240.dp))
                                Text(text = "Pan & zoom — OSM tiles live ✨", style = MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant, maxLines=1, overflow=TextOverflow.Ellipsis, softWrap=false, modifier=Modifier.padding(top=2.dp).widthIn(max=240.dp))
                                if(!hasPermission){
                                    val permInter = remember { MutableInteractionSource() }
                                    val permPressed by permInter.collectIsPressedAsState()
                                    val permScale by animateFloatAsState(targetValue = if(permPressed) 0.97f else 1f, animationSpec = if(reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label="perm")
                                    OutlinedButton(onClick = { haptic.tick(); fineLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)}, interactionSource = permInter, modifier = Modifier.padding(top=8.dp).scale(permScale).widthIn(max=200.dp), shape = RoundedCornerShape(24.dp)){ ConstrainedText(text=stringResource(R.string.map_grant_permission)) }
                                }
                            }
                        }
                        if(confettiTick>0){ ConfettiOverlay(tick=confettiTick, modifier = Modifier.fillMaxSize()) }
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha=0.9f)).padding(horizontal=8.dp, vertical=4.dp)){
                            ConstrainedText(text = if(points.isEmpty()) stringResource(R.string.map_no_route) else "${points.size} pts • ${"%.2f".format(distanceKm)} km", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            item{
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)){
                    StatOverlayCard(title = stringResource(R.string.map_stats_distance), value = "%.2f km".format(distanceKm), modifier = Modifier.weight(1f))
                    StatOverlayCard(title = stringResource(R.string.map_stats_pace), value = pace, modifier = Modifier.weight(1f))
                    StatOverlayCard(title = stringResource(R.string.map_stats_duration), value = formatDuration(durationMs), modifier = Modifier.weight(1f))
                }
            }
            item{
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)){
                    if(!isTracking){
                        val startInter = remember { MutableInteractionSource() }
                        val startPressed by startInter.collectIsPressedAsState()
                        val startScale by animateFloatAsState(targetValue = if(startPressed) 0.97f else 1f, animationSpec = if(reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label="start")
                        Button(onClick = {
                            haptic.tick()
                            if(!hasPermission){ fineLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION); return@Button }
                            if(Build.VERSION.SDK_INT>=33 && !hasNotif){ notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)}
                            localPoints.clear(); localDistance=0.0; elapsed=0L; startMs=System.currentTimeMillis()
                            ensureNotifAndStart(GpsTrackingService.ACTION_START)
                        }, interactionSource = startInter, modifier = Modifier.weight(1f).scale(startScale).widthIn(max=200.dp), shape = RoundedCornerShape(24.dp)){
                            Icon(Icons.Outlined.PlayArrow, contentDescription=null); Spacer(Modifier.width(6.dp)); ConstrainedText(text=stringResource(R.string.map_start))
                        }
                    } else {
                        val pauseInter = remember { MutableInteractionSource() }
                        val pausePressed by pauseInter.collectIsPressedAsState()
                        val pauseScale by animateFloatAsState(targetValue = if(pausePressed) 0.97f else 1f, animationSpec = if(reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label="pause")
                        OutlinedButton(onClick = { haptic.tick(); if(isPaused) ensureNotifAndStart(GpsTrackingService.ACTION_RESUME) else ensureNotifAndStart(GpsTrackingService.ACTION_PAUSE) }, interactionSource = pauseInter, modifier = Modifier.weight(1f).scale(pauseScale).widthIn(max=200.dp), shape = RoundedCornerShape(24.dp)){
                            Icon(if(isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, contentDescription=null); Spacer(Modifier.width(6.dp)); ConstrainedText(text=stringResource(if(isPaused) R.string.map_resume else R.string.map_pause))
                        }
                        val stopInter = remember { MutableInteractionSource() }
                        val stopPressed by stopInter.collectIsPressedAsState()
                        val stopScale by animateFloatAsState(targetValue = if(stopPressed) 0.97f else 1f, animationSpec = if(reduceMotion) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label="stop")
                        Button(onClick = { haptic.success(); ensureNotifAndStart(GpsTrackingService.ACTION_STOP); confettiTick++ }, interactionSource = stopInter, modifier = Modifier.weight(1f).scale(stopScale).widthIn(max=200.dp), shape = RoundedCornerShape(24.dp)){
                            Icon(Icons.Outlined.Stop, contentDescription=null); Spacer(Modifier.width(6.dp)); ConstrainedText(text=stringResource(R.string.map_stop))
                        }
                    }
                    IconButton(onClick = {
                        if(points.isEmpty()) return@IconButton
                        val gpx = GpxExporter.toGpx(points)
                        try{
                            val file = File(context.cacheDir, "route_${System.currentTimeMillis()}.gpx"); file.writeText(gpx)
                            val uri = FileProvider.getUriForFile(context, context.packageName+".fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply{ type="application/gpx+xml"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)}
                            context.startActivity(Intent.createChooser(intent, "Share GPX"))
                        }catch(_:Exception){
                            val intent = Intent(Intent.ACTION_SEND).apply{ type="text/plain"; putExtra(Intent.EXTRA_TEXT, gpx)}
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.map_share_gpx)))
                        }
                    }){ Icon(Icons.Outlined.IosShare, contentDescription=stringResource(R.string.map_share_gpx)) }
                }
            }
            item{ RouteHistorySection()}
        }
    }
}

@Composable
private fun PermissionCard(title:String, body:String, action:String, onClick:()->Unit){
    val haptic = rememberHaptics()
    val ctx = LocalContext.current
    val reduce by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
    val inter = remember { MutableInteractionSource() }
    val pressed by inter.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = if (reduce) GreenStepMotion.gentleSpring else GreenStepMotion.pressSpring, label = "permCard")
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()){
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)){
            ConstrainedText(text=title, style=MaterialTheme.typography.titleSmall, modifier=Modifier.fillMaxWidth())
            ConstrainedText(text=body, style=MaterialTheme.typography.bodySmall, maxLines=4, softWrap=true, overflow=TextOverflow.Ellipsis, modifier=Modifier.fillMaxWidth())
            OutlinedButton(onClick={ haptic.tick(); onClick() }, interactionSource = inter, modifier = Modifier.scale(scale).widthIn(max=200.dp), shape=RoundedCornerShape(24.dp)){ ConstrainedText(text=action) }
        }
    }
}

@Composable
private fun StatOverlayCard(title:String, value:String, modifier:Modifier=Modifier){
    Card(shape = RoundedCornerShape(24.dp), modifier=modifier, colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface), elevation=CardDefaults.cardElevation(defaultElevation=1.dp)){
        Column(modifier=Modifier.padding(12.dp), verticalArrangement=Arrangement.spacedBy(4.dp), horizontalAlignment=Alignment.CenterHorizontally){
            Text(text=title, style=MaterialTheme.typography.labelMedium, color=MaterialTheme.colorScheme.onSurfaceVariant, maxLines=1, overflow=TextOverflow.Ellipsis, softWrap=false)
            ConstrainedText(text=value, style=MaterialTheme.typography.titleMedium, maxLines=1)
        }
    }
}

@Composable
private fun ConfettiOverlay(tick:Int, modifier:Modifier=Modifier){
    var visible by remember(tick){ mutableStateOf(true) }
    LaunchedEffect(tick){ delay(1800); visible=false }
    if(!visible) return
    val colors = listOf(Color(0xFF4CAF50), Color(0xFFFFB74D), Color(0xFF81C784), Color(0xFFFF6F00))
    Canvas(modifier=modifier){
        val rnd = kotlin.random.Random(tick)
        repeat(18){ i ->
            val x = rnd.nextFloat()*size.width; val y = rnd.nextFloat()*size.height*0.6f + rnd.nextFloat()*40f
            val r = 6f + rnd.nextFloat()*8f; val col = colors[i%colors.size]
            drawCircle(col, radius=r, center=Offset(x,y))
        }
    }
}

@Composable
private fun RouteHistorySection(){
    val ctx = LocalContext.current
    val db = remember{ RouteDatabase.getInstance(ctx) }
    val routes by db.routeDao().observeAll().collectAsState(initial=emptyList())
    if(routes.isEmpty()) return
    Column(modifier=Modifier.fillMaxWidth(), verticalArrangement=Arrangement.spacedBy(8.dp)){
        ConstrainedText(text=stringResource(R.string.map_history_title), style=MaterialTheme.typography.titleMedium, modifier=Modifier.padding(top=4.dp))
        routes.take(5).forEach { e ->
            val conv = io.greenstep.data.map.Converters(); val pts = conv.toLatLngList(e.pointsJson)
            Card(shape=RoundedCornerShape(24.dp), modifier=Modifier.fillMaxWidth(), colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface), elevation=CardDefaults.cardElevation(defaultElevation=1.dp)){
                Row(modifier=Modifier.fillMaxWidth().padding(14.dp), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween){
                    Column(modifier=Modifier.weight(1f)){
                        ConstrainedText(text="%.2f km • %s".format(e.distanceKm, formatDuration(e.durationMs)), style=MaterialTheme.typography.titleSmall)
                        Text(text="${pts.size} pts • ${e.id.take(8)}", style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant, maxLines=1, overflow=TextOverflow.Ellipsis, softWrap=false)
                    }
                    IconButton(onClick={
                        val gpx = GpxExporter.toGpx(pts, "Route ${e.id.take(6)}")
                        try{
                            val file = File(ctx.cacheDir, "route_${e.id}.gpx"); file.writeText(gpx)
                            val uri = FileProvider.getUriForFile(ctx, ctx.packageName+".fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply{ type="application/gpx+xml"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)}
                            ctx.startActivity(Intent.createChooser(intent, "Share GPX"))
                        }catch(_:Exception){
                            val intent = Intent(Intent.ACTION_SEND).apply{ type="text/plain"; putExtra(Intent.EXTRA_TEXT, gpx)}
                            ctx.startActivity(Intent.createChooser(intent, ctx.getString(R.string.map_share_gpx)))
                        }
                    }){ Icon(Icons.Outlined.IosShare, contentDescription=null) }
                }
            }
        }
    }
}
