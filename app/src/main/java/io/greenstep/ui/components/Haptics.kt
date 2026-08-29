package io.greenstep.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

object Haptics {
    @Composable
    private fun isEnabled(): Boolean {
        val ctx = LocalContext.current
        val enabled by ThemeManagerHaptics.collect(ctx)
        return enabled
    }

    @Composable
    fun light() {
        val fb = LocalHapticFeedback.current
        val ctx = LocalContext.current
        if (!isEnabled()) return
        fb.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    @Composable
    fun medium() {
        val fb = LocalHapticFeedback.current
        if (!isEnabled()) return
        fb.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    @Composable
    fun heavy() {
        val ctx = LocalContext.current
        if (!isEnabled()) return
        val fb = LocalHapticFeedback.current
        fb.performHapticFeedback(HapticFeedbackType.LongPress)
        vibrate(ctx, 60, 180)
    }

    @Composable
    fun success() {
        val ctx = LocalContext.current
        if (!isEnabled()) return
        vibratePattern(ctx, longArrayOf(0, 40, 40, 60))
    }

    @Composable
    fun error() {
        val ctx = LocalContext.current
        if (!isEnabled()) return
        vibratePattern(ctx, longArrayOf(0, 80, 40, 80))
    }

    fun light(context: Context, vibrator: Vibrator? = null) = vibrate(context, 20, 80)
    fun medium(context: Context) = vibrate(context, 40, 120)
    fun heavy(context: Context) = vibrate(context, 60, 180)
    fun success(context: Context) = vibratePattern(context, longArrayOf(0, 40, 40, 60))
    fun error(context: Context) = vibratePattern(context, longArrayOf(0, 80, 40, 80))
}

private object ThemeManagerHaptics {
    @Composable
    fun collect(context: Context): androidx.compose.runtime.State<Boolean> {
        return io.greenstep.ui.theme.ThemeManager.hapticsEnabledFlow(context).collectAsState(initial = true)
    }
}

private fun getVibrator(context: Context): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}

private fun vibrate(context: Context, ms: Long, amp: Int) {
    val vib = getVibrator(context) ?: return
    if (!vib.hasVibrator()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(VibrationEffect.createOneShot(ms, amp))
    } else {
        @Suppress("DEPRECATION")
        vib.vibrate(ms)
    }
}

private fun vibratePattern(context: Context, pattern: LongArray) {
    val vib = getVibrator(context) ?: return
    if (!vib.hasVibrator()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(VibrationEffect.createWaveform(pattern, -1))
    } else {
        @Suppress("DEPRECATION")
        vib.vibrate(pattern, -1)
    }
}
