package androblue.common.log.db

import androblue.common.log.Category
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

@Entity
data class LoggerItemEntity(@PrimaryKey(autoGenerate = true)
                            val uid: Long?,

                            val priority: Int,
                            val category: Category,
                            val date: ZonedDateTime,
                            val thread: String,
                            val message: String) {

    fun toNiceString(formatter: DateTimeFormatter): String {
        return "${formatter.format(date)} (${logLevelToString()}) -> $message"
    }

    private fun logLevelToString(): String {
        return when (priority) {
            Log.VERBOSE -> "V"
            Log.ASSERT -> "A"
            Log.DEBUG -> "D"
            Log.ERROR -> "E"
            Log.INFO -> "I"
            Log.WARN -> "W"
            else -> "U"
        }
    }
}
