package com.ibashniak.weatherapp.network.processor

import org.junit.Test
import java.lang.AssertionError
import kotlin.test.assertEquals

class BeaufortScaleTableTest {
    private val sut = BeaufortScaleTable
    private val beaufortString: Array<String> =
        arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")

    @Test
    fun `when speed less or equal to 0,2 then beaufortString index is 0`() {
        assertEquals(
            sut.getBeaufortString(0.2, beaufortString),
            beaufortString[0]
        )
    }

    @Test
    fun `when speed more than 32,6 then beaufortString index is 12`() {
        assertEquals(
            sut.getBeaufortString(32.61, beaufortString),
            beaufortString[12]
        )
    }

    @Test
    fun `when speed less or equal to 32,6 then beaufortString index is 11`() {
        assertEquals(
            sut.getBeaufortString(32.2, beaufortString),
            beaufortString[11]
        )
    }

    @Test(expected = AssertionError::class)
    fun `when array size isn't 13 then throw AssertionError`() {
        assertEquals(
            sut.getBeaufortString(1.2, arrayOf("0", "1", "2", "3")),
            beaufortString[1]
        )
    }

    @Test(expected = AssertionError::class)
    fun `when speed is NaN then throw AssertionError`() {
        assertEquals(
            sut.getBeaufortString(Double.NaN, beaufortString),
            beaufortString[0]
        )
    }


    @Test(expected = AssertionError::class)
    fun `when speed less than 0 then throw AssertionError`() {
        assertEquals(
            sut.getBeaufortString(-1.2, beaufortString),
            beaufortString[1]
        )
    }
}