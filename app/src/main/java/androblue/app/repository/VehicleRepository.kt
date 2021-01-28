package androblue.app.repository

import androblue.app.R
import androblue.app.helper.NotificationHelper
import androblue.app.os_service.AndroBlueServiceHelper.Companion.IGNORE_REFRESH_ON_NEXT_APP_WIDGET_UPDATE
import androblue.app.repository.db.VehicleDao
import androblue.app.repository.model.VehicleModel
import androblue.app.repository.model.VehicleModelAssembler
import androblue.app.repository.model.emptyVehicleModel
import androblue.app.service.PreferenceService
import androblue.app.service.network.VehicleNetworkService
import androblue.app.service.toSystemMillis
import androblue.app.widget.AndroBlueWidget
import androblue.app.widget.isToday
import androblue.app.widget.isYesterday
import androblue.app.work.WorkManagerHelper
import androblue.common.dagger.ScopeApplication
import androblue.common.log.Logger.Builder
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.SignStyle.EXCEEDS_PAD
import org.threeten.bp.temporal.ChronoField.HOUR_OF_DAY
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR
import javax.inject.Inject
import kotlin.random.Random

@Suppress("LiftReturnOrAssignment")
@ScopeApplication
class VehicleRepository @Inject constructor(private val context: Context,
                                            private val vehicleDao: VehicleDao,
                                            private val vehicleModelAssembler: VehicleModelAssembler,
                                            private val vehicleNetworkService: VehicleNetworkService,
                                            private val preferenceService: PreferenceService,
                                            private val notificationHelper: NotificationHelper,
                                            private val workManagerHelper: WorkManagerHelper) {

    companion object {

        private val HOUR_24_FORMATTER: DateTimeFormatter = (DateTimeFormatterBuilder())
                .appendValue(HOUR_OF_DAY, 2, 2, EXCEEDS_PAD).appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .toFormatter()
                .withChronology(IsoChronology.INSTANCE)
    }

    private val vehicleStatusFlow = MutableStateFlow(StatusDataHolder(RefreshState.NOT_LOADING, emptyVehicleModel()))
    private val logger = Builder().build()

    init {
        GlobalScope.launch {
            vehicleDao.load()?.let {
                vehicleStatusFlow.value = StatusDataHolder(RefreshState.NOT_LOADING, vehicleModelAssembler.assembleWith(it))
            }
        }
    }

    fun vehicleStatus(): Flow<StatusDataHolder> = vehicleStatusFlow.asStateFlow()

    @SuppressLint("StringFormatInvalid")
    fun currentChargeStatus(): String {
        vehicleStatusFlow.value.vehicleModel.batteryLevel.let { charge ->
            logger.d("VehicleRepository currentChargeStatus:$charge")
            return context.getString(R.string.widget_chargestatus, charge)
        }
    }

    fun currentLockStatus() = vehicleStatusFlow.value.vehicleModel.lockStatus.toNiceName(context)
    fun currentClimateStatus() = vehicleStatusFlow.value.vehicleModel.climateStatus.toNiceName(context)
    fun lastRefreshTime() = prettyStringOfDateFromNow(vehicleStatusFlow.value.vehicleModel.lastUpdated)

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

        var vehicleModel = vehicleStatusFlow.value.vehicleModel
        vehicleStatusFlow.emit(StatusDataHolder(RefreshState.LOADING, vehicleModel))

        vehicleNetworkService.status(preferenceService.pin())?.result?.status?.let {
            vehicleModel = vehicleModelAssembler.assembleWith(it)
            updateDatabase(vehicleModel)
        }

        logger.d("VehicleRepository refreshStatus lockStatus:${vehicleModel.lockStatus} climateStatus:${vehicleModel.climateStatus} batteryLevel:${vehicleModel.batteryLevel}")

        vehicleStatusFlow.emit(StatusDataHolder(RefreshState.NOT_LOADING, vehicleModel))

        val result = vehicleStatusFlow.value
        logger.d("VehicleRepository refreshStatus END result:$result")

        updateWidget()

        return result
    }

    private suspend fun updateDatabase(vehicleModel: VehicleModel) {
        val entity = vehicleModelAssembler.assembleWith(vehicleDao.load()?.uid, vehicleModel)
        vehicleDao.save(entity)
    }

    suspend fun toggleLock() {
        val result = refreshStatus()
        executeLockDoors(result.vehicleModel.lockStatus == LockStatus.UNLOCKED)
    }

    suspend fun toggleClimate() {
        val result = refreshStatus()
        executeClimate(result.vehicleModel.climateStatus == ClimateStatus.OFF)
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

        if (vehicleStatusFlow.value.vehicleModel.lockStatus == LockStatus.UNLOCKED) {
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

    private fun prettyStringOfDateFromNow(date: ZonedDateTime): String {
        val now = ZonedDateTime.now()
        return when {
            date.toSystemMillis() == 0L -> context.getString(R.string.widget_updated_never)
            date.isToday(now) -> context.getString(R.string.widget_updated_today, HOUR_24_FORMATTER.format(date))
            date.isYesterday(now) -> context.getString(R.string.widget_updated_yesterday, HOUR_24_FORMATTER.format(date))
            else -> date.toString()
        }
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
}

data class StatusDataHolder(val refreshState: RefreshState,
                            val vehicleModel: VehicleModel)

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