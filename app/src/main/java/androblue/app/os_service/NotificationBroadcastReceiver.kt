package androblue.app.os_service

import androblue.app.os_service.AndroBlueServiceHelper.Companion.STOP_SERVICE_COMMAND
import androblue.common.log.Logger.Builder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationBroadcastReceiver : BroadcastReceiver() {

    private val logger = Builder().build()

    override fun onReceive(context: Context?, intent: Intent?) {
        logger.d("NotificationBroadcastReceiver onReceive intent:$intent")

        context?.startService(Intent(context, AndroBlueService::class.java).setAction(STOP_SERVICE_COMMAND))
    }
}