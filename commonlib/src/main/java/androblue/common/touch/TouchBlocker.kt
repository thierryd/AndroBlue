package androblue.common.touch

import androblue.common.log.Logger.Builder
import androblue.common.utils.UIThread
import androidx.annotation.VisibleForTesting

/**
 * The goal of this class is to block touch events.
 * ClickEvent are asynchronous in Android. Meaning that if the user double click rapidly on a button, onClick() will be called twice.
 * This can result in strange behaviour (where a Fragment is added twice for example)
 */

@VisibleForTesting
const val ENABLE_LOG = false

@Suppress("ConstantConditionIf")
object TouchBlocker {

    private val logger = Builder().build()

    /**
     * Amount of time to block momentarily any action done by the user
     */
    private const val BLOCK_MOMENTARILY_DURATION = 400L

    @VisibleForTesting
    var enabled = true

    private val UNBLOCK_TOUCH_RUNNABLE = Runnable {
        isBlockingTouchEventsMomentarily = false
    }

    private var blockTouchForever = false
    private var blockTouchForeverCount = 0
    private var isBlockingTouchEventsMomentarily = false

    fun isBlocked(): Boolean {
        var result = false

        if (enabled) {
            result = blockTouchForever || isBlockingTouchEventsMomentarily
        }

        if (ENABLE_LOG) {
            logger.d("TouchBlocker isBlocked result:$result")
        }

        return result
    }

    fun blockTouchMomentarily() {
        if (ENABLE_LOG) {
            logger.d("TouchBlocker blockTouchMomentarily ")
        }

        UIThread.removeCallbacks(UNBLOCK_TOUCH_RUNNABLE)

        isBlockingTouchEventsMomentarily = true
        UIThread.postDelayed(BLOCK_MOMENTARILY_DURATION, UNBLOCK_TOUCH_RUNNABLE)
    }

    fun blockTouchForever() {
        if (ENABLE_LOG) {
            logger.d("TouchBlocker blockTouchForever blockTouchForeverCount:$blockTouchForeverCount")
        }

        blockTouchForeverCount++
        blockTouchForever = true
    }

    fun unblockTouchForever() {
        if (ENABLE_LOG) {
            logger.d("TouchBlocker unblockTouchForever blockTouchForeverCount:$blockTouchForeverCount")
        }

        if (blockTouchForeverCount > 0) blockTouchForeverCount--

        if (blockTouchForeverCount == 0) {
            blockTouchForever = false
        }
    }

    fun clearBlockTouchForever() {
        if (ENABLE_LOG) {
            logger.d("TouchBlocker clearBlockTouchForever ")
        }

        blockTouchForeverCount = 0
        blockTouchForever = false
    }
}
