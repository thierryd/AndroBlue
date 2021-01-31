@file:Suppress("unused")

package androblue.app.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ClimateOnRequestDO(val pin: String,
                         val hvacInfo: ClimateDO)

@JsonClass(generateAdapter = true)
class ClimateDO(val airCtrl: Int,
                val defrost: Boolean,
                val heating1: Int,
                val airTemp: AirSettingsDO?)

@JsonClass(generateAdapter = true)
class AirSettingsDO(val value: String,
                    val unit: Int,
                    val hvacTempType: Int)

/**
 * Some example:
 * 18c -> {"hvacInfo":{"defrost":false,"airTemp":{"value":"04H","unit":0,"hvacTempType":0},"airCtrl":1,"heating1":0},"pin":"1234"}
 * 21.5c -> {"hvacInfo":{"defrost":false,"airTemp":{"value":"0BH","unit":0,"hvacTempType":0},"airCtrl":1,"heating1":0},"pin":"1234"}
 * 24c -> {"hvacInfo":{"defrost":false,"airTemp":{"value":"10H","unit":0,"hvacTempType":0},"airCtrl":1,"heating1":0},"pin":"1234"}
 * 28c -> {"hvacInfo":{"defrost":true,"airTemp":{"value":"18H","unit":0,"hvacTempType":0},"airCtrl":1,"heating1":1},"pin":"1234"}
 * 32c -> {"hvacInfo":{"defrost":false,"airTemp":{"value":"20H","unit":0,"hvacTempType":0},"airCtrl":1,"heating1":0},"pin":"1234"}
 */

fun climateOn(temperature: String) = ClimateDO(airCtrl = 1,
                            defrost = true,
                            heating1 = 1,
                            airTemp = AirSettingsDO(
                                    value = temperature,
                                    unit = 0,
                                    hvacTempType = 1))