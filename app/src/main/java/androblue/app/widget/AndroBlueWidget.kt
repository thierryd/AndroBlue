package androblue.app.widget

import androblue.app.AndroBlueApplication
import androblue.app.R
import androblue.app.os_service.AndroBlueService
import androblue.app.repository.VehicleRepository
import androblue.app.service.PreferenceService
import androblue.app.service.toSystemMillis
import androblue.app.widget.WidgetAction.TOGGLE_CLIMATE
import androblue.app.widget.WidgetAction.TOGGLE_LOCK
import androblue.common.log.Logger.Builder
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.SignStyle.EXCEEDS_PAD
import org.threeten.bp.temporal.ChronoField.HOUR_OF_DAY
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR
import javax.inject.Inject

class AndroBlueWidget : AppWidgetProvider() {

    companion object {
        private val HOUR_24_FORMATTER: DateTimeFormatter = (DateTimeFormatterBuilder())
                .appendValue(HOUR_OF_DAY, 2, 2, EXCEEDS_PAD).appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .toFormatter()
                .withChronology(IsoChronology.INSTANCE)
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
                setTextViewText(R.id.widget_lastupdatedate, lastRefreshTime())

                setTextViewText(R.id.widget_togglelock, context.getText(R.string.widget_togglelock))
                setTextViewText(R.id.widget_toggleclimate, context.getText(R.string.widget_toggleclimate))

                bindButton(context, TOGGLE_LOCK.name, R.id.widget_togglelock)
                bindButton(context, TOGGLE_CLIMATE.name, R.id.widget_toggleclimate)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        logger.d("AndroBlueWidget onReceive intent.action:${intent.action} intent:$intent extra:${intent.extras?.keySet()?.map { it.toString() }}")
        if (intent.action in WidgetAction.names() || intent.action == "android.appwidget.action.APPWIDGET_UPDATE") {
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

    private fun lastRefreshTime(): String {
        return prettyStringOfDateFromNow(preferenceService.lastUpdateTimeInMillis())
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
}

enum class WidgetAction {
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