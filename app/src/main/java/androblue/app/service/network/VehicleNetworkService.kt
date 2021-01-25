package androblue.app.service.network

import androblue.app.data.ClimateOffRequestDO
import androblue.app.data.ClimateOnRequestDO
import androblue.app.data.PinDO
import androblue.app.data.VehicleDO
import androblue.app.data.VehicleLockResponseDO
import androblue.app.data.VehicleResultListDO
import androblue.app.service.PreferenceService
import androblue.app.service.okhttp.ApiProvider
import androblue.common.dagger.ScopeApplication
import androblue.common.log.Logger.Builder
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject

@ScopeApplication
class VehicleNetworkService @Inject constructor(apiProvider: ApiProvider,
                                                private val accountNetworkService: AccountNetworkService,
                                                private val preferenceService: PreferenceService) {

    private var vehicleApi = apiProvider.apiClient().create(VehicleApi::class.java)
    private val logger = Builder().build()

    suspend fun vehicleId(pin: String): VehicleResultListDO? {
        logger.d("VehicleNetworkService vehicleId START")

        var result: VehicleResultListDO?
        try {
            val response: Response<VehicleResultListDO> = vehicleApi.vehicleList(PinDO(pin))
            logger.d("VehicleNetworkService vehicleId response:$response")
            result = response.body()
        } catch (e: Exception) {
            logger.e("VehicleNetworkService vehicleId e:$e", e)
            result = null
        }

        logger.d("VehicleNetworkService vehicleId END result:$result")
        return result
    }

    @Suppress("LiftReturnOrAssignment")
    suspend fun status(pin: String): VehicleDO? {
        logger.d("VehicleNetworkService status START pin.isNotEmpty:${pin.isNotEmpty()}")

        var result: VehicleDO?
        try {
            val response: Response<VehicleDO> = vehicleApi.status(preferenceService.mainVehicleId(), PinDO(pin))
            logger.d("VehicleNetworkService status response:$response")
            result = response.body()
        } catch (e: Exception) {
            logger.e("VehicleNetworkService status e:$e", e)
            result = null
        }

        logger.d("VehicleNetworkService status END result:$result")
        return result
    }

    suspend fun lock(): Boolean {
        logger.d("VehicleNetworkService lock START")

        var result = false
        try {
            accountNetworkService.getPreAuthentication()?.result?.pAuth?.let { pAuth ->
                val response = vehicleApi.lock(preferenceService.mainVehicleId(), pAuth, PinDO(preferenceService.pin()))
                logger.d("VehicleNetworkService lock response:$response")

                result = response.isSuccessful
            }
        } catch (e: Exception) {
            logger.e("VehicleNetworkService lock e:$e", e)
        }

        logger.d("VehicleNetworkService lock END result:$result")
        return result
    }

    suspend fun unlock(): Boolean {
        logger.d("VehicleNetworkService unlock START")

        var result = false
        try {
            accountNetworkService.getPreAuthentication()?.result?.pAuth?.let { pAuth ->
                val response = vehicleApi.unlock(preferenceService.mainVehicleId(), pAuth, PinDO(preferenceService.pin()))
                logger.d("VehicleNetworkService unlock response:$response")
                result = response.isSuccessful
            }
        } catch (e: Exception) {
            logger.e("VehicleNetworkService unlock e:$e", e)
        }

        logger.d("VehicleNetworkService unlock result:$result")
        return result
    }

    suspend fun climateOn(): Boolean {
        logger.d("VehicleNetworkService climateOn START")

        var result = false
        try {
            accountNetworkService.getPreAuthentication()?.result?.pAuth?.let { pAuth ->
                val response = vehicleApi.climateOn(preferenceService.mainVehicleId(), pAuth,
                                                    ClimateOnRequestDO(hvacInfo = androblue.app.data.climateOn(), pin = preferenceService.pin()))

                logger.d("VehicleNetworkService climateOn response:$response")
                result = response.isSuccessful && response.body()?.error == null
            }
        } catch (e: Exception) {
            logger.e("VehicleNetworkService climateOn e:$e", e)
        }

        logger.d("VehicleNetworkService climateOn result:$result")
        return result
    }

    suspend fun climateOff(): Boolean {
        logger.d("VehicleNetworkService climateOff START")

        var result = false
        try {
            accountNetworkService.getPreAuthentication()?.result?.pAuth?.let { pAuth ->
                val response = vehicleApi.climateOff(preferenceService.mainVehicleId(), pAuth, ClimateOffRequestDO(preferenceService.pin()))

                logger.d("VehicleNetworkService climateOff response:$response")
                result = response.isSuccessful && response.body()?.error == null
            }
        } catch (e: Exception) {
            logger.e("VehicleNetworkService climateOff e:$e", e)
        }

        logger.d("VehicleNetworkService climateOff result:$result")
        return result
    }
}

interface VehicleApi {

    @POST("/tods/api/vhcllst")
    suspend fun vehicleList(
            @Body pinDO: PinDO
    ): Response<VehicleResultListDO>

    @POST("/tods/api/lstvhclsts")
    suspend fun status(
            @Header("vehicleid") vehicleid: String,
            @Body pinDO: PinDO
    ): Response<VehicleDO>

    @POST("/tods/api/drlck")
    suspend fun lock(
            @Header("vehicleid") vehicleId: String,
            @Header("pauth") preAuthorization: String,
            @Body pinDO: PinDO
    ): Response<VehicleLockResponseDO>

    @POST("/tods/api/drulck")
    suspend fun unlock(
            @Header("vehicleid") vehicleId: String,
            @Header("pauth") preAuthorization: String,
            @Body pinDO: PinDO
    ): Response<VehicleLockResponseDO>

    @POST("/tods/api/evc/rfon")
    suspend fun climateOn(
            @Header("vehicleid") vehicleId: String,
            @Header("pauth") preAuthorization: String,
            @Body climateOnRequestDO: ClimateOnRequestDO
    ): Response<VehicleLockResponseDO>

    @POST("/tods/api/evc/rfoff")
    suspend fun climateOff(
            @Header("vehicleid") vehicleId: String,
            @Header("pauth") preAuthorization: String,
            @Body climateOffRequestDO: ClimateOffRequestDO
    ): Response<VehicleLockResponseDO>
}