package androblue.app.home

import androblue.app.BuildConfig
import androblue.app.HomeActivity
import androblue.app.R
import androblue.app.advanced.AdvancedActivity
import androblue.app.login.LoginActivity
import androblue.app.os_service.AndroBlueService
import androblue.app.repository.AccountRepository
import androblue.app.repository.RefreshState
import androblue.app.repository.VehicleRepository
import androblue.app.service.PreferenceService
import androblue.app.utils.floatRange
import androblue.app.widget.UserAction.CLIMATE_OFF
import androblue.app.widget.UserAction.CLIMATE_ON
import androblue.app.widget.UserAction.LOCK_OFF
import androblue.app.widget.UserAction.LOCK_ON
import androblue.common.dagger.ScopeActivity
import androblue.common.ext.appVersionName
import androblue.common.ext.blockingOnClickListener
import androblue.common.ext.onCreateRuns
import androblue.common.ext.onResumeRuns
import androblue.common.ext.safeCollect
import androblue.common.log.Logger.Builder
import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tiper.MaterialSpinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ScopeActivity
class HomeActivityController @Inject constructor(private val activity: HomeActivity,
                                                 private val accountRepository: AccountRepository,
                                                 private val vehicleRepository: VehicleRepository,
                                                 private val preferenceService: PreferenceService) {

    companion object {
        const val VEHICLE_CLIMATE_DEFAULT_TEMPERATURE = 23F

        @VisibleForTesting val VEHICLE_TEMPERATURES = floatRange(17, 32, 0.5F)
    }

    private var temperatureSpinnerAdapter = ArrayAdapter(activity, R.layout.home_spinner_item, VEHICLE_TEMPERATURES)

    private val temperatureItemSelectedListener = object : MaterialSpinner.OnItemSelectedListener {
        override fun onItemSelected(parent: MaterialSpinner, view: View?, position: Int, id: Long) {
            logger.d("HomeActivityController onItemSelected temperature:${VEHICLE_TEMPERATURES[position]}")
            preferenceService.climateTemperature(VEHICLE_TEMPERATURES[position])
        }

        override fun onNothingSelected(parent: MaterialSpinner) {}
    }

    private val logger = Builder().build()

    init {
        activity.onCreateRuns(::onActivityCreated)
        activity.onResumeRuns(::onActivityResumed)

        activity.lifecycleScope.launchWhenCreated {
            preferenceService.climateTemperatureFlow().safeCollect { temperature ->
                val index = VEHICLE_TEMPERATURES.indexOfFirst { it == temperature }
                if (index > -1) {
                    activity.binding.homeClimateTemperature.selection = index
                } else {
                    activity.binding.homeClimateTemperature.selection = 0
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onActivityCreated() {
        observeStates()
        setClickListeners()

        activity.binding.homeVersion.text = "v${activity.appVersionName()} - ${BuildConfig.BUILD_TYPE}"
    }

    private fun onActivityResumed() {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            vehicleRepository.refreshStatus()
        }
    }

    @SuppressLint("SetTextI18n")
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
                    homeStatustimestamp.text = "${activity.getString(R.string.widget_lastupdated)}: ${vehicleRepository.lastRefreshTime()}"
                    homeLockstatus.text = activity.getString(R.string.home_lockstatus, statusDataHolder.vehicleModel.lockStatus.toNiceName(activity))
                    homeClimatestatus.text = activity.getString(R.string.home_climatestatus, statusDataHolder.vehicleModel.climateStatus.toNiceName(activity))
                    homeBatterystatus.text = activity.getString(R.string.home_currentcharge, statusDataHolder.vehicleModel.batteryLevel)
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

            homeClimateTemperature.adapter = temperatureSpinnerAdapter
            homeClimateTemperature.onItemSelectedListener = temperatureItemSelectedListener
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
        activity.lifecycleScope.launch(Dispatchers.IO) {
            accountRepository.logout()

            withContext(Dispatchers.Main) {
                val intent = Intent(activity, LoginActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }

    private fun refreshVehicleStatus() {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            vehicleRepository.refreshStatus()
        }
    }
}