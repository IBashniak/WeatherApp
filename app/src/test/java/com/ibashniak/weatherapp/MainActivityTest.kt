package com.ibashniak.weatherapp

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

// @RunWith(RobolectricTestRunner::class)
// @Config(sdk = [Build.VERSION_CODES.M], qualifiers = "xxxhdpi")
class MainActivityTest {
    private val connectivityManager = mockk<ConnectivityManager>()

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

    @Test // ToDO simulate Build.VERSION_CODES.M
    fun `test isNetworkAvailable with Build_VERSION_CODES higher than  M`() {
        val capabilities = mockk<NetworkCapabilities>()
        every { connectivityManager.activeNetwork } returns mockk()
        every { connectivityManager.getNetworkCapabilities(any()) } returns capabilities
        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) } returns false
    }
}
