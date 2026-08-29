package io.greenstep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.greenstep.GreenStepApplication
import io.greenstep.MainActivity
import io.greenstep.R
import io.greenstep.data.day.DayRepositoryImpl
import io.greenstep.domain.usecase.GetDay
import io.greenstep.domain.usecase.IncrementStepCount
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class StepCounterService : Service(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private lateinit var controller: StepCounterController
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, getString(R.string.step_counter_channel), NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) }
            (getSystemService(NotificationManager::class.java))?.createNotificationChannel(channel)
        }
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val app = application as GreenStepApplication
        val dao = app.greenStepDatabase.dayDao()
        val repo = DayRepositoryImpl(dao)
        val getDay = GetDay(repo)
        val inc = IncrementStepCount(repo, getDay)
        controller = StepCounterController(getDay, inc, scope, app.currentDate)
        startForeground(NOTIFICATION_ID, buildNotification(controller.stats.value))
        scope.launch {
            controller.stats.collect { state ->
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIFICATION_ID, buildNotification(state))
            }
        }
        registerSensor()
    }

    private fun registerSensor() {
        val sm = sensorManager ?: return
        val counter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detector = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        counter?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        if (counter == null) detector?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    private fun buildNotification(state: StepCounterState): Notification {
        val title = resources.getQuantityString(R.plurals.step_count, state.steps, state.steps)
        val progress = if (state.goal == 0) 0 else (state.steps * 100 / state.goal).coerceIn(0, 100)
        val content = getString(R.string.step_counter_stats, state.calories, state.distanceKm, progress)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(launchIntent)
            .setSmallIcon(R.drawable.filiz_sprout)
            .setContentTitle(title)
            .setContentText(content)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .build()
    }

    private val launchIntent: PendingIntent
        get() {
            val intent = Intent(this, MainActivity::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getActivity(this, 1, intent, flags)
        }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        scope.cancel()
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val date = LocalDate.now()
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> controller.onStepCountChanged(event.values[0].toInt(), date)
            Sensor.TYPE_STEP_DETECTOR -> controller.onStepDetected(date)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        const val CHANNEL_ID = "step_counter"
        const val NOTIFICATION_ID = 1001
    }
}
