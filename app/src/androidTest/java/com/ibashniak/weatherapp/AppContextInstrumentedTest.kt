package com.ibashniak.weatherapp

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class AppContextInstrumentedTest {
    @Test
    fun appContextTest() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ibashniak.weatherapp", appContext.packageName)
    }
}
