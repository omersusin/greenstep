package io.greenstep.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.greenstep.MainActivity
import io.greenstep.R
import io.greenstep.data.day.GreenStepDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class GreenStepWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_greenstep)
            views.setTextViewText(R.id.widget_title, "Filiz 🌱")
            views.setTextViewText(R.id.widget_subtitle, "Tap to grow")
            val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
            val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, pi)
            appWidgetManager.updateAppWidget(id, views)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = GreenStepDatabase.getDatabase(context)
                    val today = LocalDate.now()
                    var steps = 0; var goal = 7500
                    try { first(db.dayDao().getDay(today))?.let { steps = it.steps; goal = it.goal } } catch (_: Exception) {}
                    val pct = (steps * 100 / goal.coerceAtLeast(1)).coerceIn(0, 100)
                    withContext(Dispatchers.Main) {
                        val rv = RemoteViews(context.packageName, R.layout.widget_greenstep)
                        rv.setTextViewText(R.id.widget_title, "$steps / $goal")
                        rv.setTextViewText(R.id.widget_subtitle, "$pct% • Filiz grows ✨")
                        rv.setProgressBar(R.id.widget_progress, 100, pct, false)
                        rv.setOnClickPendingIntent(R.id.widget_root, pi)
                        appWidgetManager.updateAppWidget(id, rv)
                    }
                } catch (_: Exception) {}
            }
        }
    }
}
