package io.greenstep.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

class HapticsController(
    private val enabled: Boolean,
    private val reduceMotion: Boolean,
    private val context: Context,
    private val feedback: HapticFeedback
) {
    fun light() {
        if (!enabled) return
        feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (!reduceMotion) vibrate(context, 20, 80)
    }
    fun tick() = light()
    fun medium() {
        if (!enabled) return
        if (reduceMotion) { feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove); return }
        feedback.performHapticFeedback(HapticFeedbackType.LongPress)
        vibrate(context, 40, 120)
    }
    fun heavy() {
        if (!enabled) return
        if (reduceMotion) { feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove); return }
        feedback.performHapticFeedback(HapticFeedbackType.LongPress)
        vibrate(context, 60, 180)
    }
    fun success() {
        if (!enabled) return
        if (reduceMotion) { feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove); return }
        vibratePattern(context, longArrayOf(0, 40, 40, 60))
    }
    fun error() {
        if (!enabled) return
        if (reduceMotion) { feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove); return }
        vibratePattern(context, longArrayOf(0, 80, 40, 80))
    }
}

@Composable
fun rememberHaptics(): HapticsController {
    val ctx = LocalContext.current
    val fb = LocalHapticFeedback.current
    val enabled by io.greenstep.ui.theme.ThemeManager.hapticsEnabledFlow(ctx).collectAsState(initial = true)
    val reduce by io.greenstep.ui.theme.ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
    return remember(enabled, reduce, ctx, fb) { HapticsController(enabled, reduce, ctx, fb) }
}

object Haptics {
    @Composable
    private fun isEnabled(): Boolean {
        val ctx = LocalContext.current
        val enabled by ThemeManagerHaptics.collectHaptics(ctx)
        return enabled
    }
    @Composable
    private fun isReduceMotion(): Boolean {
        val ctx = LocalContext.current
        val reduce by ThemeManagerHaptics.collectReduceMotion(ctx)
        return reduce
    }
    @Composable
    fun light() {
        val fb = LocalHapticFeedback.current
        val ctx = LocalContext.current
        if (!isEnabled()) return
        fb.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (!isReduceMotion()) vibrate(ctx, 20, 80)
    }
    @Composable
    fun medium() {
        val fb = LocalHapticFeedback.current
        val ctx = LocalContext.current
        if (!isEnabled()) return
        if (isReduceMotion()) { fb.performHapticFeedback(HapticFeedbackType.TextHandleMove); return }
        fb.performHapticFeedback(HapticFeedbackType.LongPress)
        vibrate(ctx, 40, 120)
    }
    @Composable
    fun heavy() {
        val ctx = LocalContext.current
        if (!isEnabled()) return
        val fb = LocalHapticFeedback.current
        if (isReduceMotion()) { fb.performHapticFeedback(HapticFeedbackType.TextHandleMove); return }
        fb.performHapticFeedback(HapticFeedbackType.LongPress)
        vibrate(ctx, 60, 180)
    }
    @Composable
    fun success() {
        val ctx = LocalContext.current
        if (!isEnabled()) return
        if (isReduceMotion()) { LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove); return }
        vibratePattern(ctx, longArrayOf(0, 40, 40, 60))
    }
    @Composable
    fun error() {
        val ctx = LocalContext.current
        if (!isEnabled()) return
        if (isReduceMotion()) { LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove); return }
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
    fun collectHaptics(context: Context): androidx.compose.runtime.State<Boolean> {
        return io.greenstep.ui.theme.ThemeManager.hapticsEnabledFlow(context).collectAsState(initial = true)
    }
    @Composable
    fun collectReduceMotion(context: Context): androidx.compose.runtime.State<Boolean> {
        return io.greenstep.ui.theme.ThemeManager.reduceMotionFlow(context).collectAsState(initial = false)
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
