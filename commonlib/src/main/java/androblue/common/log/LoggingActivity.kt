package androblue.common.log

import androblue.common.ext.className
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

abstract class LoggingActivity : AppCompatActivity {

    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    private val logger = Logger.Builder().category(Category.LIFECYCLE).build()

    private val simpleClassName = className

    init {
        @Suppress("LeakingThis")
        logger.v("activity instantiate $simpleClassName $this")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.v("activity onCreate $simpleClassName savedInstanceState:$savedInstanceState $this")
        super.onCreate(savedInstanceState)
    }

    override fun onRestart() {
        logger.v("activity onRestart $simpleClassName $this")
        super.onRestart()
    }

    override fun onStart() {
        logger.v("activity onStart $simpleClassName $this")
        super.onStart()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        logger.v("activity onRestoreInstanceState $simpleClassName $this")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onResume() {
        logger.v("activity onResume $simpleClassName $this")
        super.onResume()
    }

    //////////////////////
    // Activity is running and visible
    //////////////////////

    override fun onConfigurationChanged(newConfig: Configuration) {
        logger.v("activity onConfigurationChanged $simpleClassName $this")
        super.onConfigurationChanged(newConfig)
    }

    //////////////////////

    override fun onSaveInstanceState(outState: Bundle) {
        logger.v("activity onSaveInstanceState $simpleClassName $this")
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        logger.v("activity onPause $simpleClassName $this")
        super.onPause()
    }

    override fun onStop() {
        logger.v("activity onStop $simpleClassName $this")
        super.onStop()
    }

    override fun onDestroy() {
        logger.v("activity onDestroy $simpleClassName $this")
        super.onDestroy()
    }
}
