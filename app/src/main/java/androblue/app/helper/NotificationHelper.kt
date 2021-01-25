package androblue.app.helper

import androblue.app.R
import androblue.app.os_service.AndroBlueServiceHelper.Companion.STOP_SERVICE_COMMAND
import androblue.app.os_service.NotificationBroadcastReceiver
import androblue.common.dagger.ScopeApplication
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import javax.inject.Inject

@ScopeApplication
class NotificationHelper @Inject constructor(private val application: Application) {

    companion object {
        const val FOREGROUND_SERVICE_NOTIFICATION_ID = 999
        const val FOREGROUND_SERVICE_NOTIFICATION_CHANNEL = "General"

        private const val APP_NOTIFICATION_ID = 666
    }

    private val notificationManager = NotificationManagerCompat.from(application)

    init {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel(FOREGROUND_SERVICE_NOTIFICATION_CHANNEL,
                                                                              FOREGROUND_SERVICE_NOTIFICATION_CHANNEL,
                                                                              NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    fun showAppNotification(title: String) {
        val notification = NotificationCompat.Builder(application, FOREGROUND_SERVICE_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle(title)
                .setContentText(title)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        notificationManager.notify(APP_NOTIFICATION_ID, notification)
    }

    fun buildNotificationService(title: String): Notification {
        val intent = Intent(application, NotificationBroadcastReceiver::class.java)
        intent.action = STOP_SERVICE_COMMAND
        val pendingIntent = PendingIntent.getBroadcast(application, 0, intent, 0)

        return NotificationCompat.Builder(application, FOREGROUND_SERVICE_NOTIFICATION_CHANNEL)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notif)
                .setOngoing(true)
                .setNotificationSilent()
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(0, 0, true)
                .addAction(NotificationCompat.Action.Builder(R.drawable.ic_notif, application.getString(R.string.notification_stop), pendingIntent).build())
                .build()
    }

    fun updateServiceNotification(title: String) {
        val notification = buildNotificationService(title)
        notificationManager.notify(FOREGROUND_SERVICE_NOTIFICATION_ID, notification)
    }
}