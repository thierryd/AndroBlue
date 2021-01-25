package androblue.app.repository

import androblue.app.R
import androblue.app.data.VehicleStatusDO
import androblue.app.helper.NotificationHelper
import androblue.app.os_service.AndroBlueServiceHelper.Companion.IGNORE_REFRESH_ON_NEXT_APP_WIDGET_UPDATE
import androblue.app.service.PreferenceService
import androblue.app.service.network.VehicleNetworkService
import androblue.app.widget.AndroBlueWidget
import androblue.app.work.WorkManagerHelper
import androblue.common.dagger.ScopeApplication
import androblue.common.log.Logger.Builder
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import kotlin.random.Random

@Suppress("LiftReturnOrAssignment")
@ScopeApplication
class VehicleRepository @Inject constructor(private val context: Context,
                                            private val vehicleNetworkService: VehicleNetworkService,
                                            private val preferenceService: PreferenceService,
                                            private val notificationHelper: NotificationHelper,
                                            private val workManagerHelper: WorkManagerHelper) {

    private val logger = Builder().build()

    private val vehicleStatusFlow = MutableStateFlow(StatusDataHolder(RefreshState.NOT_LOADING, LockStatus.UNKNOWN, ClimateStatus.UNKNOWN, 0))

    fun vehicleStatus(): Flow<StatusDataHolder> = vehicleStatusFlow.asStateFlow()

    @SuppressLint("StringFormatInvalid")
    fun currentChargeStatus(): String {
        vehicleStatusFlow.value.batteryLevel.let { charge ->
            logger.d("VehicleRepository currentChargeStatus:$charge")
            return context.getString(R.string.widget_chargestatus, charge)
        }
    }

    fun currentLockStatus() = vehicleStatusFlow.value.lockStatus.toNiceName(context)

    fun currentClimateStatus() = vehicleStatusFlow.value.climateStatus.toNiceName(context)

    suspend fun validatePin(pin: String): Boolean {
        val status = vehicleNetworkService.vehicleId(pin)
        val vehicleId = status?.result?.vehicles?.firstOrNull()?.vehicleId

        if (vehicleId != null) {
            preferenceService.mainVehicleId(vehicleId)
        }

        return vehicleId != null
    }

    suspend fun refreshStatus(): StatusDataHolder {
        logger.d("VehicleRepository refreshStatus START")

        vehicleStatusFlow.emit(StatusDataHolder(RefreshState.LOADING,
                                                vehicleStatusFlow.value.lockStatus,
                                                vehicleStatusFlow.value.climateStatus,
                                                vehicleStatusFlow.value.batteryLevel))

        val status = vehicleNetworkService.status(preferenceService.pin())

        val lockStatus = lockStatus(status?.result?.status)
        val climateStatus = climateStatus(status?.result?.status)
        val batteryLevel = batteryLevel(status?.result?.status)

        logger.d("VehicleRepository refreshStatus lockStatus:$lockStatus climateStatus:$climateStatus batteryLevel:$batteryLevel")

        preferenceService.setLastUpdateTime(ZonedDateTime.now())

        vehicleStatusFlow.emit(StatusDataHolder(RefreshState.NOT_LOADING, lockStatus, climateStatus, batteryLevel))

        val result = vehicleStatusFlow.value
        logger.d("VehicleRepository refreshStatus END result:$result")

        updateWidget()

        return result
    }

    suspend fun toggleLock() {
        val result = refreshStatus()
        executeLockDoors(result.lockStatus == LockStatus.UNLOCKED)
    }

    suspend fun toggleClimate() {
        val result = refreshStatus()
        executeClimate(result.climateStatus == ClimateStatus.OFF)
    }

    suspend fun executeLockDoors(lockDoors: Boolean): Boolean {
        logger.d("VehicleRepository executeLockDoors lockDoors:$lockDoors")

        var title = when {
            lockDoors -> context.getString(R.string.notif_locking_door)
            else -> context.getString(R.string.notif_unlockingdoor_door)
        }
        notificationHelper.updateServiceNotification(title)

        val result = if (lockDoors) {
            vehicleNetworkService.lock()
        } else {
            vehicleNetworkService.unlock()
        }

        title = when {
            lockDoors -> {
                when {
                    result -> context.getString(R.string.notif_lockdoor_success)
                    else -> context.getString(R.string.notif_lockdoor_failure)
                }
            }
            else -> {
                when {
                    result -> context.getString(R.string.notif_unlockdoor_success)
                    else -> context.getString(R.string.notif_unlockdoor_failure)
                }
            }
        }

        notificationHelper.showAppNotification(title)

        workManagerHelper.enqueueRefresh()
        logger.d("VehicleRepository executeLockDoors lockDoors:$lockDoors END result:$result")
        return result
    }

    suspend fun executeClimate(climateOn: Boolean): Boolean {
        logger.d("VehicleRepository executeClimate climateOn:$climateOn")

        var title = when {
            climateOn -> context.getString(R.string.notif_climateon)
            else -> context.getString(R.string.notif_climateoff)
        }
        notificationHelper.updateServiceNotification(title)

        if (vehicleStatusFlow.value.lockStatus == LockStatus.UNLOCKED) {
            title = context.getString(R.string.notif_error_climatecannotbeturnon_doorunlocked)
            logger.d(title)
            notificationHelper.showAppNotification(title)

            return false
        }

        val result = if (climateOn) {
            vehicleNetworkService.climateOn()
        } else {
            vehicleNetworkService.climateOff()
        }

        title = when {
            climateOn -> {
                when {
                    result -> context.getString(R.string.notif_climateon_success)
                    else -> context.getString(R.string.notif_climateon_failure)
                }
            }
            else -> {
                when {
                    result -> context.getString(R.string.notif_climateoff_success)
                    else -> context.getString(R.string.notif_climateoff_failure)
                }
            }
        }

        notificationHelper.showAppNotification(title)

        workManagerHelper.enqueueRefresh()
        logger.d("VehicleRepository executeClimate climateOn:$climateOn END result:$result")
        return result
    }

    private fun updateWidget() {
        val intent = Intent(context, AndroBlueWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, AndroBlueWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        intent.putExtra("Random", Random.nextInt())
        intent.putExtra(IGNORE_REFRESH_ON_NEXT_APP_WIDGET_UPDATE, true)

        logger.d("VehicleRepository updateWidget intent:$intent extras:${intent.extras}")

        context.sendBroadcast(intent)
    }

    private fun lockStatus(status: VehicleStatusDO?): LockStatus {
        return when {
            status == null -> return LockStatus.UNKNOWN
            status.doorLock == null -> return LockStatus.UNKNOWN
            status.doorLock == true -> LockStatus.LOCKED
            else -> LockStatus.UNLOCKED
        }
    }

    private fun climateStatus(status: VehicleStatusDO?): ClimateStatus {
        return when {
            status == null -> return ClimateStatus.UNKNOWN
            status.airCtrlOn == null -> return ClimateStatus.UNKNOWN
            status.airCtrlOn == true -> ClimateStatus.ON
            else -> ClimateStatus.OFF
        }
    }

    private fun batteryLevel(status: VehicleStatusDO?): Int {
        return when (status) {
            null -> 0
            else -> status.evStatus?.batteryStatus ?: 0
        }
    }
}

data class StatusDataHolder(val refreshState: RefreshState,
                            val lockStatus: LockStatus,
                            val climateStatus: ClimateStatus,
                            val batteryLevel: Int)

enum class LockStatus {
    UNKNOWN, LOCKED, UNLOCKED;

    fun toNiceName(context: Context): String {
        return when (this) {
            UNKNOWN -> context.getString(R.string.widget_statusunknown)
            LOCKED -> context.getString(R.string.widget_lockstatuson)
            UNLOCKED -> context.getString(R.string.widget_lockstatusoff)
        }
    }
}

enum class ClimateStatus {
    UNKNOWN, ON, OFF;

    fun toNiceName(context: Context): String {
        return when (this) {
            UNKNOWN -> context.getString(R.string.widget_statusunknown)
            ON -> context.getString(R.string.widget_climatestatuson)
            OFF -> context.getString(R.string.widget_climatestatusoff)
        }
    }
}