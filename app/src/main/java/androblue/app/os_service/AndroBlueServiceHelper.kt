package androblue.app.os_service

import androblue.app.AndroBlueApplication
import androblue.app.R
import androblue.app.helper.NotificationHelper
import androblue.app.helper.NotificationHelper.Companion.FOREGROUND_SERVICE_NOTIFICATION_ID
import androblue.app.repository.AccountRepository
import androblue.app.repository.VehicleRepository
import androblue.app.service.PreferenceService
import androblue.app.widget.UserAction.CLIMATE_OFF
import androblue.app.widget.UserAction.CLIMATE_ON
import androblue.app.widget.UserAction.LOCK_OFF
import androblue.app.widget.UserAction.LOCK_ON
import androblue.app.widget.UserAction.TOGGLE_CLIMATE
import androblue.app.widget.UserAction.TOGGLE_LOCK
import androblue.common.log.Logger.Builder
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AndroBlueServiceHelper @Inject constructor(private val androBlueService: AndroBlueService) {

    companion object {
        const val STOP_SERVICE_COMMAND = "STOP_SERVICE_COMMAND"
        const val IGNORE_REFRESH_ON_NEXT_APP_WIDGET_UPDATE = "IGNORE_REFRESH_ON_NEXT_APP_WIDGET_UPDATE"

        private val _25_SECONDS_IN_MS = TimeUnit.SECONDS.toMillis(25L)
    }

    @Inject lateinit var context: Context
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var vehicleRepository: VehicleRepository
    @Inject lateinit var accountRepository: AccountRepository
    @Inject lateinit var preferenceService: PreferenceService

    private val logger = Builder().build()

    fun onCreate() {
        (androBlueService.application as AndroBlueApplication).getAndroBlueApplicationComponent().inject(this)
    }

    fun onStartCommand(intent: Intent) {
        val usernameNotEmpty = preferenceService.username().isNotEmpty()
        logger.d("AndroBlueServiceHelper onStartCommand usernameNotEmpty:$usernameNotEmpty intent.action:${intent.action} extras:${intent.extras?.keySet()?.map { it.toString() }}")

        if (intent.action == STOP_SERVICE_COMMAND) {
            logger.d("AndroBlueServiceHelper onStartCommand - STOP_SERVICE_COMMAND stopping service")
            androBlueService.stopForeground(true)
        } else {
            logger.d("AndroBlueServiceHelper onStartCommand starting - foreground service")
            androBlueService.startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notificationHelper.buildNotificationService(context.getString(R.string.updating_widget)))

            if (usernameNotEmpty) {
                GlobalScope.launch(Dispatchers.IO) {
                    val loggedIn = ensureLoggedIn()

                    logger.d("AndroBlueServiceHelper onStartCommand  - loggedIn:$loggedIn")
                    if (loggedIn) {
                        when (intent.action) {
                            LOCK_ON.name -> vehicleRepository.executeLockDoors(true)
                            LOCK_OFF.name -> vehicleRepository.executeLockDoors(false)
                            TOGGLE_LOCK.name -> vehicleRepository.toggleLock()
                            CLIMATE_ON.name -> vehicleRepository.executeClimate(true)
                            CLIMATE_OFF.name -> vehicleRepository.executeClimate(false)
                            TOGGLE_CLIMATE.name -> vehicleRepository.toggleClimate()
                            "android.appwidget.action.APPWIDGET_UPDATE" -> onAppUpdateWidgetRequest(intent)
                            else -> logger.d("AndroBlueServiceHelper onStartCommand unknown command:${intent.action}")
                        }
                    } else {
                        notificationHelper.showAppNotification(context.getString(R.string.notif_login_error))
                    }

                    logger.d("AndroBlueServiceHelper onStartCommand  - all done. Stopping foreground service")
                    androBlueService.stopForeground(true)
                }
            } else {
                logger.d("AndroBlueServiceHelper onStartCommand - username not set. Stopping foreground service")
                androBlueService.stopForeground(true)
            }
        }
    }

    private suspend fun onAppUpdateWidgetRequest(intent: Intent) {
        val ignoreNextRefresh = intent.extras?.getBoolean(IGNORE_REFRESH_ON_NEXT_APP_WIDGET_UPDATE, false) == true
        logger.d("AndroBlueServiceHelper onAppUpdateWidgetRequest - ignoreNextRefresh:$ignoreNextRefresh")
        if (!ignoreNextRefresh) {
            logger.d("AndroBlueServiceHelper onAppUpdateWidgetRequest - refreshing vehicle status")
            vehicleRepository.refreshStatus()
        }
    }

    private suspend fun ensureLoggedIn(): Boolean {
        val start = System.currentTimeMillis()

        while (!accountRepository.loggedInStatus().loggedIn) {
            logger.d("AndroBlueServiceHelper waiting for user to be logged in")
            delay(16L)

            if (System.currentTimeMillis() - start > _25_SECONDS_IN_MS) {
                return false
            }
        }

        return true
    }
}