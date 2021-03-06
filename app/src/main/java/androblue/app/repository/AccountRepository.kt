package androblue.app.repository

import androblue.app.repository.db.VehicleDao
import androblue.app.repository.model.VehicleModelAssembler
import androblue.app.service.PreferenceService
import androblue.app.service.network.AccountNetworkService
import androblue.app.service.network.LoginResult
import androblue.common.TimeService
import androblue.common.dagger.ScopeApplication
import androblue.common.ext.toSystemMillis
import androblue.common.log.Logger.Builder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@ScopeApplication
class AccountRepository @Inject constructor(private val preferenceService: PreferenceService,
                                            private val vehicleDao: VehicleDao,
                                            private val vehicleModelAssembler: VehicleModelAssembler,
                                            private val timeService: TimeService,
                                            private val accountNetworkService: AccountNetworkService,
                                            private val vehicleRepository: VehicleRepository) {

    private val loggedInStatusFlow = MutableStateFlow(AccountStatus(RefreshState.LOADING, false))
    private val logger = Builder().build()

    init {
        if (preferenceService.userAccessToken().isNotEmpty()) {
            val tokenExpireAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(preferenceService.userAccessTokenExpireAt()), timeService.defaultZoneId())
            if (tokenExpireAt.isBefore(timeService.now())) {
                onRefreshTokenExpired()
            } else {
                logger.d("AccountRepository token is still valid. Now:${timeService.now()} tokenExpireAt:$tokenExpireAt")
                loggedInStatusFlow.tryEmit(AccountStatus(RefreshState.NOT_LOADING, true))
            }
        }
    }

    fun loggedInStatus(): AccountStatus = loggedInStatusFlow.value
    fun loggedInStatusObs(): Flow<AccountStatus> = loggedInStatusFlow.asStateFlow()

    suspend fun login(username: String, password: String, pin: String): AccountStatus {
        logger.d("AccountRepository login START")
        val result = accountNetworkService.login(username, password)
        logger.d("AccountRepository login result:$result")

        var loginValid = false
        if (result.loginResult is LoginResult.LoginSuccess) {
            logger.d("AccountRepository login success - validating pin")

            //We need to set the user access token info since it is used by validatePin()
            val expireAt = timeService.now().plusSeconds(result.loginDO?.result?.expireIn ?: 0L)
            preferenceService.userAccessToken(result.loginDO?.result?.accessToken ?: "")
            preferenceService.userAccessTokenExpireAt(expireAt.toSystemMillis())

            if (vehicleRepository.validatePin(pin)) {
                logger.d("AccountRepository login - pin is valid")

                preferenceService.username(username)
                preferenceService.password(username)
                preferenceService.pin(pin)

                loginValid = true
            }
        }

        loggedInStatusFlow.emit(AccountStatus(RefreshState.NOT_LOADING, loginValid))

        logger.d("AccountRepository login END loginValid:${loginValid} current status:${loggedInStatus()}")

        return loggedInStatus()
    }

    suspend fun logout() {
        logger.d("AccountRepository logout ")

        vehicleDao.clear()

        preferenceService.username("")
        preferenceService.password("")
        preferenceService.pin("")
        preferenceService.mainVehicleId("")

        preferenceService.userAccessToken("")
        preferenceService.userAccessTokenExpireAt(0L)

        loggedInStatusFlow.emit(AccountStatus(RefreshState.NOT_LOADING, false))
    }

    private fun onRefreshTokenExpired() {
        GlobalScope.launch {
            logger.d("AccountRepository token has expired. Re-loging")
            loggedInStatusFlow.emit(AccountStatus(RefreshState.LOADING, false))
            preferenceService.userAccessToken("") //clear the access token

            do {
                val result = login(preferenceService.username(), preferenceService.password(), preferenceService.pin())
                logger.d("AccountRepository onRefreshTokenExpired ")
                if (!result.loggedIn) {
                    delay(5000L)
                }
            } while (!loggedInStatus().loggedIn)
        }
    }
}

data class AccountStatus(val state: RefreshState, val loggedIn: Boolean)

