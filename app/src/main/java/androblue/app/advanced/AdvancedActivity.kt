package androblue.app.advanced

import androblue.app.databinding.AdvancedActivityBinding
import androblue.common.log.LoggingActivity
import androblue.common.utils.ActivityUtils
import android.os.Bundle
import dagger.android.AndroidInjection
import javax.inject.Inject

class AdvancedActivity : LoggingActivity() {

    @Suppress("unused")
    @Inject lateinit var advancedActivityController: AdvancedActivityController

    lateinit var binding: AdvancedActivityBinding

    override fun onStart() {
        super.onStart()

        ActivityUtils.onActivityStarted(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        binding = AdvancedActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}