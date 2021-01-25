package androblue.common.touch

import android.view.View
import android.view.View.OnClickListener

class BlockingOnClickListener(private val block: () -> Unit) : OnClickListener {

    override fun onClick(v: View) {
        if (TouchBlocker.isBlocked()) {
            return
        }

        //IMPORTANT: invoke must be before blockTouchMomentarily(), as some BlockingOnClickListener() calls Activity.onBackPressed() directly
        block.invoke()

        TouchBlocker.blockTouchMomentarily()
    }
}