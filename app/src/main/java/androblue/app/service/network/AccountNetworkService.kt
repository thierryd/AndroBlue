package androblue.app.service.network

import androblue.app.data.LoginDO
import androblue.app.data.LoginResponseDO
import androblue.app.data.PinDO
import androblue.app.data.PreAuthorizationResponseDO
import androblue.app.service.PreferenceService
import androblue.app.service.network.LoginResult.LoginGeneralError
import androblue.app.service.network.LoginResult.LoginSuccess
import androblue.app.service.okhttp.ApiProvider
import androblue.common.dagger.ScopeApplication
import androblue.common.log.Logger
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject

@ScopeApplication
class AccountNetworkService @Inject constructor(apiProvider: ApiProvider,
                                                private val preferenceService: PreferenceService) {

    private var accountApi = apiProvider.apiClient().create(AccountApi::class.java)
    private val logger = Logger.Builder().build()

    @Suppress("LiftReturnOrAssignment")
    suspend fun login(username: String, password: String): LoginResultHolder {
        logger.d("AccountNetworkService login START")

        var result: LoginResultHolder
        try {
            val response = accountApi.login(LoginDO(username, password))
            logger.d("AccountNetworkService login response:$response")
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.error == null) {
                    result = LoginResultHolder(response.body(), LoginSuccess)
                } else {
                    result = LoginResultHolder(null, LoginGeneralError)
                }
            } else {
                result = LoginResultHolder(null, LoginGeneralError)
            }
        } catch (e: Exception) {
            logger.e("AccountNetworkService login error:$e", e)
            result = LoginResultHolder(null, LoginGeneralError)
        }

        logger.d("AccountNetworkService login END result:$result")

        return result
    }

    @Suppress("LiftReturnOrAssignment")
    suspend fun getPreAuthentication(): PreAuthorizationResponseDO? {
        logger.d("AccountNetworkService getPreAuthentication START")

        var result: PreAuthorizationResponseDO?
        try {
            val response: Response<PreAuthorizationResponseDO> = accountApi.getPreAuthentication(PinDO(preferenceService.pin()))
            logger.d("AccountNetworkService getPreAuthentication response:$response")
            result = response.body()
        } catch (e: Exception) {
            logger.e("AccountNetworkService getPreAuthentication e:$e", e)
            result = null
        }
        logger.d("AccountNetworkService getPreAuthentication END result:$result")
        return result
    }
}

data class LoginResultHolder(val loginDO: LoginResponseDO?, val loginResult: LoginResult)

sealed class LoginResult {
    object LoginSuccess : LoginResult()
    object LoginGeneralError : LoginResult()
}

interface AccountApi {

    @POST("/tods/api/lgn")
    suspend fun login(@Body loginDO: LoginDO): Response<LoginResponseDO>

    @POST("/tods/api/vrfypin")
    suspend fun getPreAuthentication(@Body pinDO: PinDO): Response<PreAuthorizationResponseDO>
}