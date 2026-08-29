package io.greenstep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.greenstep.R
import io.greenstep.data.map.Converters
import io.greenstep.data.map.LatLng
import io.greenstep.data.map.RouteDatabase
import io.greenstep.data.map.haversineKm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GpsTrackingService : Service() {
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var fusedClient: Any? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var startTimeMs: Long = 0L
    private var lastTickKm: Double = 0.0
    private var isPausedInternal = false

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() { super.onCreate(); createChannel() }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForegroundTracking(isResume = false)
            ACTION_RESUME -> startForegroundTracking(isResume = true)
            ACTION_PAUSE -> pauseTracking()
            ACTION_STOP -> { stopTrackingAndPersist(); stopSelf() }
            else -> startForegroundTracking(isResume = false)
        }
        return START_STICKY
    }
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "GPS Tracking", NotificationManager.IMPORTANCE_LOW)
            ch.description = "Tracks your route"
            (getSystemService(NotificationManager::class.java))?.createNotificationChannel(ch)
        }
    }
    private fun buildNotification(distanceKm: Double, paused: Boolean = false): Notification {
        val txt = if (paused) getString(R.string.map_tracking_paused) else "%.2f km • %s".format(distanceKm, getString(R.string.map_tracking_text))
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (paused) getString(R.string.map_tracking_paused) else getString(R.string.map_tracking_title))
            .setContentText(txt)
            .setSmallIcon(R.drawable.filiz_sprout)
            .setOngoing(!paused)
            .setOnlyAlertOnce(true)
            .build()
    }
    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        try { nm.notify(NOTIFICATION_ID, buildNotification(_distance.value, isPausedInternal)) } catch (_: Exception) {}
    }
    private fun tickHapticIfNeeded(newDistance: Double) {
        if (newDistance - lastTickKm >= 0.1) {
            lastTickKm = newDistance
            vibrateTick()
            updateNotification()
        }
    }
    private fun vibrateTick() {
        try {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as? Vibrator
            } ?: return
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vib.vibrate(VibrationEffect.createOneShot(30, 80))
            else @Suppress("DEPRECATION") vib.vibrate(30)
        } catch (_: Exception) {}
    }
    private fun onLocation(loc: Location) {
        if (isPausedInternal) return
        val p = LatLng(loc.latitude, loc.longitude)
        val cur = _points.value.toMutableList()
        if (cur.isNotEmpty()) {
            val d = haversineKm(cur.last(), p)
            if (d < 0.002) return
        }
        cur.add(p)
        _points.value = cur
        var dist = 0.0
        for (i in 1 until cur.size) dist += haversineKm(cur[i-1], cur[i])
        _distance.value = dist
        _duration.value = System.currentTimeMillis() - startTimeMs
        tickHapticIfNeeded(dist)
        if (cur.size % 5 == 0) updateNotification()
    }
    private fun tryStartFused(): Boolean {
        return try {
            val cls = Class.forName("com.google.android.gms.location.LocationServices")
            val method = cls.getMethod("getFusedLocationProviderClient", android.content.Context::class.java)
            val client = method.invoke(null, this) ?: return false
            fusedClient = client
            val priorityCls = Class.forName("com.google.android.gms.location.Priority")
            val prioHigh = priorityCls.getField("PRIORITY_HIGH_ACCURACY").getInt(null)
            val reqBuilderCls = Class.forName("com.google.android.gms.location.LocationRequest\$Builder")
            val builder = try {
                reqBuilderCls.getConstructor(Integer.TYPE, Long::class.javaPrimitiveType).newInstance(prioHigh, 3000L)
            } catch (_: Exception) {
                reqBuilderCls.getConstructor(Long::class.javaPrimitiveType).newInstance(3000L)
            }
            try { reqBuilderCls.getMethod("setMinUpdateIntervalMillis", Long::class.javaPrimitiveType).invoke(builder, 2000L) } catch (_: Exception) {}
            try { reqBuilderCls.getMethod("setMinUpdateDistanceMeters", java.lang.Float.TYPE).invoke(builder, 2f) } catch (_: Exception) {}
            val req = reqBuilderCls.getMethod("build").invoke(builder)
            val callbackCls = Class.forName("com.google.android.gms.location.LocationCallback")
            val callback = java.lang.reflect.Proxy.newProxyInstance(callbackCls.classLoader, arrayOf(callbackCls)) { _, m, args ->
                if (m.name == "onLocationResult" && args != null && args.isNotEmpty()) {
                    val result = args[0]
                    val locs = result.javaClass.getMethod("getLocations").invoke(result) as? List<*>
                    locs?.forEach { o -> if (o is Location) onLocation(o) }
                }
                null
            }
            val looper = android.os.Looper.getMainLooper()
            client.javaClass.getMethod("requestLocationUpdates", req.javaClass, callbackCls, android.os.Looper::class.java).invoke(client, req, callback, looper)
            true
        } catch (_: Exception) { false } catch (_: Throwable) { false }
    }
    private fun startWithLocationManager() {
        try {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager = lm
            val listener = LocationListener { loc: Location -> onLocation(loc) }
            locationListener = listener
            try { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 2f, listener) } catch (_: Exception) {}
            try { lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 5f, listener) } catch (_: Exception) {}
        } catch (_: SecurityException) {} catch (_: Exception) {}
    }
    private fun startForegroundTracking(isResume: Boolean) {
        if (!isResume) {
            _points.value = emptyList()
            _distance.value = 0.0
            _duration.value = 0L
            startTimeMs = System.currentTimeMillis()
            lastTickKm = 0.0
            _isTracking.value = true
        }
        isPausedInternal = false
        _isPaused.value = false
        val notif = buildNotification(_distance.value, false)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            else startForeground(NOTIFICATION_ID, notif)
        } catch (_: Exception) {
            try { startForeground(NOTIFICATION_ID, notif) } catch (_: Exception) {}
        }
        if (!isResume) {
            if (!tryStartFused()) startWithLocationManager()
        } else {
            if (fusedClient == null && locationManager == null) {
                if (!tryStartFused()) startWithLocationManager()
            }
        }
        updateNotification()
    }
    private fun pauseTracking() {
        isPausedInternal = true
        _isPaused.value = true
        removeLocationUpdates()
        updateNotification()
    }
    private fun removeLocationUpdates() {
        try {
            fusedClient?.let { client ->
                try { client.javaClass.getMethod("removeLocationUpdates", Class.forName("com.google.android.gms.location.LocationCallback")).invoke(client, null) } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        try { locationListener?.let { locationManager?.removeUpdates(it) } } catch (_: Exception) {}
    }
    private fun stopTrackingAndPersist() {
        removeLocationUpdates()
        val pts = _points.value
        val dist = _distance.value
        val dur = if (startTimeMs == 0L) 0L else System.currentTimeMillis() - startTimeMs
        if (pts.size >= 2) {
            scope.launch {
                try {
                    val db = RouteDatabase.getInstance(applicationContext)
                    val conv = Converters()
                    val id = System.currentTimeMillis().toString()
                    val entity = io.greenstep.data.map.RouteEntity(id = id, pointsJson = conv.fromLatLngList(pts), distanceKm = dist, durationMs = dur, saved = true)
                    db.routeDao().upsert(entity)
                } catch (_: Exception) {}
            }
        }
        _isTracking.value = false
        _isPaused.value = false
        isPausedInternal = false
        locationManager = null
        locationListener = null
        fusedClient = null
        try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (_: Exception) { @Suppress("DEPRECATION") stopForeground(true) }
    }
    companion object {
        const val CHANNEL_ID = "gps_tracking"
        const val NOTIFICATION_ID = 1002
        const val ACTION_START = "start"
        const val ACTION_PAUSE = "pause"
        const val ACTION_RESUME = "resume"
        const val ACTION_STOP = "stop"
        private val _points = MutableStateFlow<List<LatLng>>(emptyList())
        val points: StateFlow<List<LatLng>> = _points
        private val _distance = MutableStateFlow(0.0)
        val distance: StateFlow<Double> = _distance
        private val _duration = MutableStateFlow(0L)
        val duration: StateFlow<Long> = _duration
        private val _isTracking = MutableStateFlow(false)
        val isTracking: StateFlow<Boolean> = _isTracking
        private val _isPaused = MutableStateFlow(false)
        val isPaused: StateFlow<Boolean> = _isPaused
    }
}
