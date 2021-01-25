package androblue.common.log.printer

import androblue.common.log.Category
import androblue.common.log.Logger.Companion.loggerDatabaseHelper
import androblue.common.log.Logger.Companion.timeService
import androblue.common.log.util.StackTraceUtil
import org.threeten.bp.ZonedDateTime

class DatabasePrinter(private val category: Category) {

    fun log(priority: Int, message: String, throwable: Throwable?) {
        timeService?.let { timeService ->
            loggerDatabaseHelper?.apply {
                val now = ZonedDateTime.now(timeService.defaultZoneId())

                logToDatabase(priority, category, now, Thread.currentThread().name, message)

                if (throwable != null) {
                    StackTraceUtil.prettyPrintStackTrace(throwable).lines().forEach {
                        logToDatabase(priority, category, now, Thread.currentThread().name, it)
                    }
                }
            }
        }
    }
}