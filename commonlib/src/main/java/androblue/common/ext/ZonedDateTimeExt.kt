package androblue.common.ext

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

fun ZonedDateTime.toSystemMillis() = toInstant().toEpochMilli()
fun Long.toZonedDateTime(zoneId: ZoneId): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)
