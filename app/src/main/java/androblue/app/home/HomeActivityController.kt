package androblue.app.home

import androblue.app.HomeActivity
import androblue.app.R
import androblue.app.advanced.AdvancedActivity
import androblue.app.login.LoginActivity
import androblue.app.os_service.AndroBlueService
import androblue.app.repository.AccountRepository
import androblue.app.repository.RefreshState
import androblue.app.repository.VehicleRepository
import androblue.app.service.PreferenceService
import androblue.app.widget.WidgetAction.CLIMATE_OFF
import androblue.app.widget.WidgetAction.CLIMATE_ON
import androblue.app.widget.WidgetAction.LOCK_OFF
import androblue.app.widget.WidgetAction.LOCK_ON
import androblue.common.dagger.ScopeActivity
import androblue.common.ext.appVersionName
import androblue.common.ext.blockingOnClickListener
import androblue.common.ext.onCreateRuns
import androblue.common.log.Logger.Builder
import android.annotation.SuppressLint
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ScopeActivity
class HomeActivityController @Inject constructor(private val activity: HomeActivity,
                                                 private val accountRepository: AccountRepository,
                                                 private val vehicleRepository: VehicleRepository,
                                                 private val preferenceService: PreferenceService) {

    private val logger = Builder().build()

    init {
        activity.onCreateRuns(::onActivityCreated)
    }

    @SuppressLint("SetTextI18n")
    private fun onActivityCreated() {
        observeStates()
        setClickListeners()

        activity.binding.homeVersion.text = "v${activity.appVersionName()}"
    }

    private fun observeStates() {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            accountRepository.loggedInStatusObs().collect {
                activity.binding.apply {
                    homeLoggedInAs.text = activity.getString(R.string.home_loggininas, preferenceService.username())
                }
            }
        }

        activity.lifecycleScope.launch(Dispatchers.Main) {
            vehicleRepository.vehicleStatus().collect { statusDataHolder ->
                activity.binding.apply {
                    homeLockstatus.text = activity.getString(R.string.home_lockstatus, statusDataHolder.lockStatus.toNiceName(activity))
                    homeClimatestatus.text = activity.getString(R.string.home_climatestatus, statusDataHolder.climateStatus.toNiceName(activity))
                    homeBatterystatus.text = activity.getString(R.string.home_currentcharge, statusDataHolder.batteryLevel)
                    homeStatusprogress.isVisible = statusDataHolder.refreshState == RefreshState.LOADING
                }
            }
        }

        refreshVehicleStatus()
    }

    private fun setClickListeners() {
        activity.binding.apply {
            homeLogout.blockingOnClickListener(::logout)
            homeStatusrefresh.blockingOnClickListener(::refreshVehicleStatus)

            homeLock.blockingOnClickListener { setLockStatus(true) }
            homeUnlock.blockingOnClickListener { setLockStatus(false) }

            homeClimateOn.blockingOnClickListener { switchClimate(true) }
            homeClimateOff.blockingOnClickListener { switchClimate(false) }

            homeAdvanced.blockingOnClickListener {
                val intent = Intent(activity, AdvancedActivity::class.java)
                activity.startActivity(intent)
            }
        }
    }

    private fun setLockStatus(lock: Boolean) {
        logger.d("HomeActivityController setLockStatus lock:$lock")
        val serviceIntent = Intent(activity, AndroBlueService::class.java).apply {
            action = if (lock) LOCK_ON.name else LOCK_OFF.name
        }
        ContextCompat.startForegroundService(activity, serviceIntent)
    }

    private fun switchClimate(turnClimateOn: Boolean) {
        logger.d("HomeActivityController switchClimate turnClimateOn:$turnClimateOn")

        val serviceIntent = Intent(activity, AndroBlueService::class.java).apply {
            action = if (turnClimateOn) CLIMATE_ON.name else CLIMATE_OFF.name
        }
        ContextCompat.startForegroundService(activity, serviceIntent)
    }

    private fun logout() {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            accountRepository.logout()

            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    private fun refreshVehicleStatus() {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            vehicleRepository.refreshStatus()
        }
    }
}