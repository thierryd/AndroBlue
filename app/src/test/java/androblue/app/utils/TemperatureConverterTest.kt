package androblue.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TemperatureConverterTest {

    private val temperatureConverter= TemperatureConverter()

    @Test
    fun test_convert () {
        assertEquals("04H", temperatureConverter.convert(18F))
        assertEquals("0BH", temperatureConverter.convert(21.5F))
        assertEquals("10H", temperatureConverter.convert(24F))
        assertEquals("18H", temperatureConverter.convert(28F))
        assertEquals("20H", temperatureConverter.convert(32F))
    }
}