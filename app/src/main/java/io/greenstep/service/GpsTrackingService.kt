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
import androidx.core.app.NotificationCompat
import io.greenstep.R

class GpsTrackingService : Service() {

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_RESUME -> startForegroundTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_STOP -> { stopTracking(); stopSelf() }
            else -> startForegroundTracking()
        }
        return START_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "GPS Tracking", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Tracks your route"
            (getSystemService(NotificationManager::class.java))?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.map_tracking_title))
            .setContentText(getString(R.string.map_tracking_text))
            .setSmallIcon(R.drawable.filiz_sprout)
            .setOngoing(true)
            .build()
    }

    @Suppress("MissingPermission")
    private fun startForegroundTracking() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        try {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager = lm
            val listener = LocationListener { _: Location -> }
            locationListener = listener
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, listener)
        } catch (_: SecurityException) {
        } catch (_: Exception) {}
    }

    private fun pauseTracking() {
        try { locationListener?.let { locationManager?.removeUpdates(it) } } catch (_: Exception) {}
    }

    private fun stopTracking() {
        try { locationListener?.let { locationManager?.removeUpdates(it) } } catch (_: Exception) {}
        locationManager = null
        locationListener = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    companion object {
        const val CHANNEL_ID = "gps_tracking"
        const val NOTIFICATION_ID = 1002
        const val ACTION_START = "start"
        const val ACTION_PAUSE = "pause"
        const val ACTION_RESUME = "resume"
        const val ACTION_STOP = "stop"
    }
}
