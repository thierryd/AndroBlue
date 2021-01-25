package androblue.app.advanced

import androblue.common.dagger.ScopeActivity
import androblue.common.ext.blockingOnClickListener
import androblue.common.ext.onCreateRuns
import android.content.Intent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import javax.inject.Inject

@ScopeActivity
class AdvancedActivityController @Inject constructor(private val activity: AdvancedActivity,
                                                     private val shareLogsDelegate: ShareLogsDelegate) {

    init {
        activity.onCreateRuns(::onActivityCreated)
    }

    private fun onActivityCreated() {
        setClickListeners()
    }

    private fun setClickListeners() {
        activity.binding.apply {
            advancedSharelogs.blockingOnClickListener { shareLogsDelegate.shareLogs() }
            advancedLicense.blockingOnClickListener { activity.startActivity(Intent(activity, OssLicensesMenuActivity::class.java)) }
            advancedCrash.blockingOnClickListener { throw RuntimeException("Test Crash") }
        }
    }
}