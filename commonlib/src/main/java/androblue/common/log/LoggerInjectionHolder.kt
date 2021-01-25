package androblue.common.log

import androblue.common.TimeService
import androblue.common.log.db.LoggerDatabaseHelper
import javax.inject.Inject

class LoggerInjectionHolder {

    @Inject lateinit var loggerDatabaseHelper: LoggerDatabaseHelper
    @Inject lateinit var timeService: TimeService
}