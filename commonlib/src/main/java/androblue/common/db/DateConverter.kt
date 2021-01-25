package androblue.common.db

import androidx.room.TypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class DateConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): ZonedDateTime? {
        return value?.let { ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), defaultZoneId()) }
    }

    @TypeConverter
    fun dateToTimestamp(date: ZonedDateTime?): Long? {
        return date?.toInstant()?.toEpochMilli()
    }

    private fun defaultZoneId(): ZoneId = ZoneId.systemDefault()
}

