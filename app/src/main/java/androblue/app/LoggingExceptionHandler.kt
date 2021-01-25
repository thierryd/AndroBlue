package androblue.app

import androblue.common.log.Logger
import androblue.common.log.db.LoggerDatabaseHelper
import java.lang.Thread.UncaughtExceptionHandler

class LoggingExceptionHandler(private val defaultExceptionHandler: UncaughtExceptionHandler,
                              private val loggerDatabaseHelper: LoggerDatabaseHelper) : UncaughtExceptionHandler {

    private val logger = Logger.Builder().build()

    override fun uncaughtException(t: Thread, e: Throwable) {
        logger.e("LoggingExceptionHandler uncaught exception:$e", e)
        loggerDatabaseHelper.flush()
        defaultExceptionHandler.uncaughtException(t, e)
    }
}