package androblue.app.os_service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AndroBlueService : Service() {

    private val androBlueServiceHelper = AndroBlueServiceHelper(this)

    override fun onCreate() {
        super.onCreate()
        androBlueServiceHelper.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        androBlueServiceHelper.onStartCommand(intent)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}