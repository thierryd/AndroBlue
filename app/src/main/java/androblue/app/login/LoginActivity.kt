package androblue.app.login

import androblue.app.databinding.LoginActivityBinding
import androblue.app.service.PreferenceService
import androblue.common.log.LoggingActivity
import androblue.common.utils.ActivityUtils
import android.os.Bundle
import dagger.android.AndroidInjection
import javax.inject.Inject

class LoginActivity : LoggingActivity() {

    lateinit var binding: LoginActivityBinding

    @Inject lateinit var preferenceService: PreferenceService

    @Suppress("unused")
    @Inject lateinit var loginActivityController: LoginActivityController

    override fun onStart() {
        super.onStart()

        ActivityUtils.onActivityStarted(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        binding = LoginActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}