package androblue.app.work

import androblue.app.AndroBlueApplication
import androblue.common.TimeService
import androblue.common.log.Logger.Builder
import androblue.common.log.db.LoggerDao
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CleanupWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @Inject lateinit var timeService: TimeService
    @Inject lateinit var loggerDao: LoggerDao

    private val logger = Builder().build()

    override suspend fun doWork(): Result {
        logger.d("CleanupWorker doWork ")

        ((context.applicationContext) as AndroBlueApplication).getAndroBlueApplicationComponent().inject(this)

        withContext(Dispatchers.IO) {
            val thresholdDate = timeService.now().minusDays(5)
            loggerDao.cleanup(thresholdDate)
        }

        return Result.success()
    }
}