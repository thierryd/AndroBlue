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

fun climateOn() = ClimateDO(airCtrl = 1,
                            defrost = true,
                            heating1 = 1,
                            airTemp = AirSettingsDO(
                                    value = "1AH",
                                    unit = 0,
                                    hvacTempType = 1))