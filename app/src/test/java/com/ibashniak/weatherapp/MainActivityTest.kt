package com.ibashniak.weatherapp

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val connectivityManager = mockk<ConnectivityManager>()

class MainActivityTest {

    @Test
    fun `test isNetworkAvailable with Build_VERSION_CODES less than  M`() {
        every { connectivityManager.activeNetworkInfo } returns mockk<NetworkInfo>().apply {
            every { isConnected } returns true
        }

        Assert.assertEquals(
            true,
            MainActivity.isNetworkAvailable(connectivityManager)
        )

        every { connectivityManager.activeNetworkInfo } returns mockk<NetworkInfo>().apply {
            every { isConnected } returns false
        }

        Assert.assertEquals(
            false,
            MainActivity.isNetworkAvailable(connectivityManager)
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.M], qualifiers = "xxxhdpi")
class MainActivityInstrumentedTest {
    private val capabilities = mockk<NetworkCapabilities>()

    @Before
    fun setup() {
        every { connectivityManager.activeNetwork } returns mockk()
        every { connectivityManager.getNetworkCapabilities(any()) } returns capabilities
        every { capabilities.hasTransport(any()) } returns false
    }

    @Test
    fun `when WIFI is available then network isn available`() {

        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        Assert.assertEquals(true, MainActivity.isNetworkAvailable(connectivityManager))
    }

    @Test
    fun `when carrier service is available then network isn available`() {
        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        Assert.assertEquals(true, MainActivity.isNetworkAvailable(connectivityManager))
    }

    @Test
    fun `when ETHERNET is available then network isn available`() {

        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns true
        Assert.assertEquals(true, MainActivity.isNetworkAvailable(connectivityManager))
    }

    @Test
    fun `when BLUETOOTH is available then network isn available`() {
        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) } returns true
        Assert.assertEquals(true, MainActivity.isNetworkAvailable(connectivityManager))
    }

    @Test
    fun `when NetworkCapabilities are not available then network isn't available`() {
        Assert.assertEquals(false, MainActivity.isNetworkAvailable(connectivityManager))
    }
}
