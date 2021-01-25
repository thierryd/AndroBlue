package androblue.common.log.printer

import androblue.common.BuildConfig
import androblue.common.log.Category
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

open class LogcatPrinter constructor(private val category: Category) {

    fun log(priority: Int, message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                Log.println(priority, category.logcatTag, message)
            } else {
                when (priority) {
                    Log.VERBOSE -> Log.e(category.logcatTag, message, throwable)
                    Log.DEBUG -> Log.e(category.logcatTag, message, throwable)
                    Log.INFO -> Log.e(category.logcatTag, message, throwable)
                    Log.WARN -> Log.e(category.logcatTag, message, throwable)
                    Log.ERROR -> Log.e(category.logcatTag, message, throwable)
                }

                if (throwable is UnknownHostException) {
                    //UnknownHostException do not log stacktrace on logcat (special case in Log.getStackTraceString()
                    //So we need to force a stacktrace
                    Log.e(category.logcatTag, throwable.unknownHostExceptionStackTrace())
                }
            }
        } else if (priority == Log.ERROR) {
            Log.e(category.logcatTag, message, throwable)
        }
    }
}

fun UnknownHostException.unknownHostExceptionStackTrace(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    printStackTrace(pw)
    pw.flush()
    return sw.toString()
}
