package androblue.common.utils

import androblue.common.log.Logger
import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting

@Suppress("ConstantConditionIf")
object UIThread {

    object Setup {
        @VisibleForTesting var isRunningUnitTest = false
    }

    private const val SHOULD_LOG = false
    private val handler = Handler(Looper.getMainLooper())

    fun post(runnable: Runnable) {
        if (SHOULD_LOG) {
            Logger.Builder().build().d("UIThread post runnable:$runnable")
        }

        handler.post(runnable)
    }

    fun postDelayed(delayMillis: Long, runnable: Runnable) {
        if (SHOULD_LOG) {
            Logger.Builder().build().d("UIThread postDelayed post runnable:$runnable")
        }

        handler.postDelayed(runnable, delayMillis)
    }

    fun post(block: () -> Unit) {
        if (SHOULD_LOG) {
            Logger.Builder().build().d("UIThread post runnable:$block")
        }

        handler.post(block)
    }

    fun postDelayed(delayMillis: Long, block: () -> Unit) {
        if (SHOULD_LOG) {
            Logger.Builder().build().d("UIThread post runnable:$block")
        }

        handler.postDelayed(block, delayMillis)
    }

    fun removeCallbacks(runnable: Runnable) {
        if (SHOULD_LOG) {
            Logger.Builder().build().d("UIThread postDelayed removeCallbacks:$runnable")
        }

        handler.removeCallbacks(runnable)
    }
}