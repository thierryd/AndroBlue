package androblue.app.widget

import androblue.app.AndroBlueApplication
import androblue.app.R
import androblue.app.os_service.AndroBlueService
import androblue.app.repository.VehicleRepository
import androblue.app.service.PreferenceService
import androblue.app.widget.UserAction.TOGGLE_CLIMATE
import androblue.app.widget.UserAction.TOGGLE_LOCK
import androblue.common.log.Logger.Builder
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class AndroBlueWidget : AppWidgetProvider() {

    companion object {
        private const val START_APP = "START_APP"
    }

    @Inject lateinit var context: Context
    @Inject lateinit var vehicleRepository: VehicleRepository
    @Inject lateinit var preferenceService: PreferenceService

    private val logger = Builder().build()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        logger.d("AndroBlueWidget onUpdate")

        (context.applicationContext as AndroBlueApplication).getAndroBlueApplicationComponent().inject(this)

        appWidgetIds.forEach { appWidgetId ->
            val views: RemoteViews = RemoteViews(context.packageName, R.layout.widget).apply {
                setTextViewText(R.id.widget_charge, vehicleRepository.currentChargeStatus())
                setTextViewText(R.id.widget_lockstatus, vehicleRepository.currentLockStatus())
                setTextViewText(R.id.widget_climatstatus, vehicleRepository.currentClimateStatus())
                setTextViewText(R.id.widget_lastupdated, context.getText(R.string.widget_lastupdated))
                setTextViewText(R.id.widget_lastupdatedate, vehicleRepository.lastRefreshTime())

                setTextViewText(R.id.widget_togglelock, context.getText(R.string.widget_togglelock))
                setTextViewText(R.id.widget_toggleclimate, context.getText(R.string.widget_toggleclimate))

                bindButton(context, START_APP, R.id.widget_labelcontainer)
                bindButton(context, TOGGLE_LOCK.name, R.id.widget_togglelock)
                bindButton(context, TOGGLE_CLIMATE.name, R.id.widget_toggleclimate)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        logger.d("AndroBlueWidget onReceive intent.action:${intent.action} intent:$intent extra:${intent.extras?.keySet()?.map { it.toString() }}")

        if (intent.action == START_APP) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            context.startActivity(launchIntent)
        } else if (intent.action in UserAction.names() || intent.action == "android.appwidget.action.APPWIDGET_UPDATE") {
            val serviceIntent = Intent(context, AndroBlueService::class.java).apply {
                action = intent.action
                intent.extras?.let { putExtras(it) }
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }

    private fun RemoteViews.bindButton(context: Context, action: String, buttonId: Int) {
        val intent = Intent(context, AndroBlueWidget::class.java)
        intent.action = action
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        setOnClickPendingIntent(buttonId, pendingIntent)
    }
}

enum class UserAction {
    LOCK_ON, LOCK_OFF, TOGGLE_LOCK, CLIMATE_ON, CLIMATE_OFF, TOGGLE_CLIMATE;

    companion object {
        fun names() = values().map { it.name }
    }
}

fun ZonedDateTime.isToday(now: ZonedDateTime): Boolean {
    val nowDay = now.dayOfMonth
    val nowMonth = now.monthValue
    val nowYear = now.year

    val otherZonedDateTime = this.withZoneSameInstant(now.zone)
    val otherDay = otherZonedDateTime.dayOfMonth
    val otherMonth = otherZonedDateTime.monthValue
    val otherYear = otherZonedDateTime.year

    if (nowDay == otherDay && nowMonth == otherMonth && nowYear == otherYear) {
        return true
    }

    return false
}

fun ZonedDateTime.isYesterday(now: ZonedDateTime): Boolean {
    return isToday(now.minusDays(1L))
}