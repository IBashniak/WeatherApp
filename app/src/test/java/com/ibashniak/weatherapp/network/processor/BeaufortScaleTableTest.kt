package com.ibashniak.weatherapp.network.processor

import org.junit.Test
import java.lang.AssertionError
import org.junit.Assert.assertEquals

class BeaufortScaleTableTest {
    private val sut = BeaufortScaleTable
    private val beaufortString: Array<String> =
        arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")

    @Test
    fun `when speed less or equal to 0,2 then beaufortString index is 0`() {
        assertEquals(
            beaufortString[0],
            sut.getBeaufortString(0.2, beaufortString)
        )
    }

    @Test
    fun `when speed more than 32,6 then beaufortString index is 12`() {
        assertEquals(
            beaufortString[12],
            sut.getBeaufortString(32.61, beaufortString)
        )
    }

    @Test
    fun `when speed less or equal to 32,6 then beaufortString index is 11`() {
        assertEquals(
            beaufortString[11],
            sut.getBeaufortString(32.2, beaufortString)
        )
    }

    @Test(expected = AssertionError::class)
    fun `when array has improper size then throw AssertionError`() {
        sut.getBeaufortString(1.2, arrayOf("0", "1", "2", "3"))
    }

    @Test(expected = AssertionError::class)
    fun `when speed is NaN then throw AssertionError`() {
        sut.getBeaufortString(Double.NaN, beaufortString)
    }


    @Test(expected = AssertionError::class)
    fun `when speed is negative then throw AssertionError`() {
        sut.getBeaufortString(-1.2, beaufortString)
    }
}