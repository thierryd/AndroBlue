package androblue.common.log.util

import androblue.common.log.Logger
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Utility related with stack trace.
 */
object StackTraceUtil {

    private val STACK_TRACE_ORIGIN: String

    init {
        val loggerClassName = Logger::class.java.name
        STACK_TRACE_ORIGIN = loggerClassName.substring(0, loggerClassName.lastIndexOf('.') + 1)
    }

    fun prettyPrintStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw, true)
        throwable.printStackTrace(pw)
        return sw.buffer.toString()
    }
}
