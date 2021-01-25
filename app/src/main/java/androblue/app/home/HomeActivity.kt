package androblue.app

import androblue.app.databinding.HomeActivityBinding
import androblue.app.home.HomeActivityController
import androblue.common.log.LoggingActivity
import androblue.common.utils.ActivityUtils
import android.os.Bundle
import dagger.android.AndroidInjection
import javax.inject.Inject

class HomeActivity : LoggingActivity() {

    @Inject lateinit var mainActivityController: HomeActivityController

    lateinit var binding: HomeActivityBinding

    override fun onStart() {
        super.onStart()

        ActivityUtils.onActivityStarted(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        binding = HomeActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}