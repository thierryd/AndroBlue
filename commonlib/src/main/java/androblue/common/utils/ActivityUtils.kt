package androblue.common.utils

import androblue.common.BuildConfig
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.POWER_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP
import android.os.PowerManager.FULL_WAKE_LOCK
import android.os.PowerManager.ON_AFTER_RELEASE
import android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
import android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON

object ActivityUtils {

    fun onActivityStarted(activity: Activity) {
        if (BuildConfig.DEBUG && hasRiseAndShinePermission(activity)) {
            riseAndShine(activity)
        }
    }

    /**
     * Show the activity over the lockscreen and wake up the device. If you launched the app2.app manually
     * both of these conditions are already true. If you deployed from the IDE, however, this will
     * save you from hundreds of power button presses and pattern swiping per day!
     *
     *
     * See https://gist.github.com/androidfu/3ae247867b68d6ead7dc
     */
    @Suppress("DEPRECATION")
    @SuppressLint("InvalidWakeLockTag")
    private fun riseAndShine(activity: Activity) {
        activity.runOnUiThread {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    activity.setShowWhenLocked(true)
                    activity.setTurnScreenOn(true)
                } else {
                    @Suppress("DEPRECATION")
                    activity.window.addFlags(FLAG_SHOW_WHEN_LOCKED)
                    @Suppress("DEPRECATION")
                    activity.window.addFlags(FLAG_TURN_SCREEN_ON)
                }

                val powerManager =
                    activity.application.getSystemService(POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                        FULL_WAKE_LOCK or SCREEN_BRIGHT_WAKE_LOCK or ACQUIRE_CAUSES_WAKEUP or ON_AFTER_RELEASE,
                        "Wakeup!"
                )

                wakeLock.acquire(10000L)

                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                requestDismissKeyguard(activity)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun requestDismissKeyguard(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
    }

    private fun hasRiseAndShinePermission(activity: Activity): Boolean {
        val hasDisableKeyguardPermission =
            hasPermission(activity, Manifest.permission.DISABLE_KEYGUARD)
        val hasWakelockPermission = hasPermission(activity, Manifest.permission.WAKE_LOCK)

        return hasDisableKeyguardPermission && hasWakelockPermission
    }

    //for example, permission can be "android.permission.WRITE_EXTERNAL_STORAGE"
    private fun hasPermission(activity: Activity, permission: String): Boolean {
        return activity.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}
