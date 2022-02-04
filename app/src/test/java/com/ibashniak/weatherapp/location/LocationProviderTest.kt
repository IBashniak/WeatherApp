package com.ibashniak.weatherapp.location

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationProviderTest {
    private val locationRequest = mockk<LocationRequest>()
    private val locationChannel = mockk<LocationChannel>().apply {
        justRun { send(any()) }
    }
    private val location = mockk<Location>().apply {
        every { latitude } returns 125.488
    }
    private val fusedLocationProviderClient = mockk<FusedLocationProviderClient>().apply {
        every { requestLocationUpdates(locationRequest, any(), any()) } returns mockk()
        every { removeLocationUpdates(any<LocationCallback>()) } returns mockk()
    }
    private val locationResult = mockk<LocationResult>().apply {
        every { lastLocation } returns location
        every { locations } returns emptyList<Location>()
    }

    private val sut = LocationProvider(
        mockk(),
        mockk(),
        locationChannel,
        fusedLocationProviderClient,
        locationRequest
    )

    @Test
    fun requestLocationUpdates() {
        sut.startLocationUpdates()
        verify(exactly = 1) {
            fusedLocationProviderClient.requestLocationUpdates(
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun stopLocationUpdates() {
        sut.stopLocationUpdates()
        verify(exactly = 1) { fusedLocationProviderClient.removeLocationUpdates(any<LocationCallback>()) }
    }

    @Test
    fun getLocationChannel() {
        assertEquals(locationChannel, sut.locationChannel)
    }

    @Test
    fun buildLocationCallBack() {
        val cb = sut.buildLocationCallBack()
        cb.onLocationResult(locationResult)
        verify(exactly = 1) { locationChannel.send(any()) }
    }
}
