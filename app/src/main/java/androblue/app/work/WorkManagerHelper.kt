package androblue.app.work

import androblue.app.AndroBlueApplication
import androblue.app.repository.VehicleRepository
import androblue.common.log.Logger.Builder
import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerHelper @Inject constructor(private val application: Application) {

    private val logger = Builder().build()

    fun enqueueRefresh() {
        logger.d("WorkManagerHelper enqueueRefresh ")

        WorkManager.getInstance(application).run {
            enqueue(refreshWork(1))
            enqueue(refreshWork(2))
        }
    }

    private fun refreshWork(delayInMinutes: Long) = OneTimeWorkRequestBuilder<RefreshWorker>()
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .build()
}

class RefreshWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @Inject lateinit var vehicleRepository: VehicleRepository

    private val logger = Builder().build()

    override suspend fun doWork(): Result {
        logger.d("RefreshWorker doWork ")

        ((context.applicationContext) as AndroBlueApplication).getAndroBlueApplicationComponent().inject(this)

        withContext(Dispatchers.IO) {
            vehicleRepository.refreshStatus()
        }

        return Result.success()
    }
}