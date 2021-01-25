package androblue.app

import androblue.app.login.LoginActivity
import androblue.app.service.PreferenceService
import androblue.common.log.LoggingActivity
import androblue.common.utils.ActivityUtils
import android.content.Intent
import android.os.Bundle
import dagger.android.AndroidInjection
import javax.inject.Inject

class SplashActivity : LoggingActivity() {

    @Inject lateinit var preferenceService: PreferenceService

    override fun onStart() {
        super.onStart()

        ActivityUtils.onActivityStarted(this)
    }

    @Suppress("LiftReturnOrAssignment")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        val intent: Intent
        if (preferenceService.username().isEmpty()) {
            intent = Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
        } else {
            intent = Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
        }

        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }
}