package androblue.common.ext

import androblue.common.touch.BlockingOnClickListener
import android.view.View

fun View.blockingOnClickListener(clickListener: () -> Unit) {
    setOnClickListener(BlockingOnClickListener(clickListener))
}