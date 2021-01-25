package androblue.common.ext

import org.threeten.bp.ZonedDateTime

fun ZonedDateTime.toSystemMillis() = toInstant().toEpochMilli()