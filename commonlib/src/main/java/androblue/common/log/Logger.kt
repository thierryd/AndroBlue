package androblue.common.log

import androblue.common.TimeService
import androblue.common.log.Category.NONE
import androblue.common.log.db.LoggerDatabaseHelper
import androblue.common.log.printer.DatabasePrinter
import androblue.common.log.printer.LogcatPrinter
import android.util.Log
import kotlin.math.min

@Suppress("unused")
class Logger private constructor(category: Category) {

    private val logcatPrinter = LogcatPrinter(category)
    private val databasePrinter = DatabasePrinter(category)

    fun v(message: String) {
        log(Log.VERBOSE, message, null)
    }

    fun d(message: String) {
        log(Log.DEBUG, message, null)
    }

    fun w(message: String, throwable: Throwable) {
        log(Log.WARN, message, throwable)
    }

    fun e(message: String) {
        log(Log.ERROR, message, null)
    }

    fun e(message: String, throwable: Throwable) {
        log(Log.ERROR, message, throwable)
    }

    private fun log(priority: Int, message: String, throwable: Throwable?) {
        logcatPrinter.log(priority, message, throwable)
        databasePrinter.log(priority, message, throwable)
    }

    class Builder {
        private var category: Category = NONE

        fun category(category: Category): Builder {
            this.category = category
            return this
        }

        fun build(): Logger {
            return Logger(category)
        }
    }

    companion object {
        internal var loggerDatabaseHelper: LoggerDatabaseHelper? = null
        internal var timeService: TimeService? = null

        fun initialize(loggerInjectionHolder: LoggerInjectionHolder) {
            loggerDatabaseHelper = loggerInjectionHolder.loggerDatabaseHelper
            timeService = loggerInjectionHolder.timeService
        }

        private val doubleWhitespaceRegex = "\\s+".toRegex()

        fun substringSafe(obj: Any): String {
            val tmpStr = obj.toString()
                    .replace("\n", " ")
                    .replace(doubleWhitespaceRegex, " ")

            return tmpStr.substring(0, min(200, tmpStr.length))
        }
    }
}