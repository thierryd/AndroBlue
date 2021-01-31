package androblue.app.utils

import okhttp3.internal.toHexString
import java.util.Locale
import javax.inject.Inject

/**
 * From https://github.com/Hacksore/bluelinky/blob/develop/lib/util.ts
 *
 * Converts Kia's stupid temp codes to celsius
 * From what I can tell it uses a hex index on a list of temperatures starting at 14c ending at 30c with an added H on the end,
 * I'm thinking it has to do with Heat/Cool H/C but needs to be tested, while the car is off, it defaults to 01H
 */
class TemperatureConverter @Inject constructor() {

    private val temperatures = floatRange(16, 32, 0.5F)

    fun convert(temperature: Float): String {
        val index = temperatures.indexOf(temperature)
        val hexCode = index.toHexString()
        return hexCode.toUpperCase(Locale.ENGLISH).padStart(2, padChar = '0') + 'H'
    }
}

fun floatRange(start: Int, end: Int, step: Float): Array<Float> {
    val values = arrayListOf<Float>().apply {
        var i = start.toFloat()
        while (i <= end) {
            add(i)
            i += step
        }
    }
    return values.toTypedArray()
}