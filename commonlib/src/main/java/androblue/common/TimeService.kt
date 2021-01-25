package androblue.common

import androblue.common.dagger.ScopeApplication
import org.threeten.bp.Clock
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@ScopeApplication
class TimeService @Inject constructor() {

    private var clock: Clock = Clock.system(defaultZoneId())

    /**
     * Return now
     *
     * @return the current time which can be a real one or a fixed one
     */
    fun now(): ZonedDateTime = ZonedDateTime.now(clock)

    fun defaultZoneId(): ZoneId = ZoneId.systemDefault()
}