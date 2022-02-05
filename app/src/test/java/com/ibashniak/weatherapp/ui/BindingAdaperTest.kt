package com.ibashniak.weatherapp.ui

import android.view.View
import android.view.ViewPropertyAnimator
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class BindingAdaperTest {

    private val viewropertyAnimator = mockk<ViewPropertyAnimator>().apply {
        every { rotation(any()) } returns mockk<ViewPropertyAnimator>().apply {
            every { setDuration(any()) } returns mockk()
        }
    }

    private val view = mockk<View>().apply {
        justRun { setVisibility(any()) }
        every { animate() } returns viewropertyAnimator
        every { setRotation(any()) } returns mockk()
    }

    @Test
    fun isLoadingTest() {
        isLoading(view, true)
        verify { view.setVisibility(View.VISIBLE) }

        isLoading(view, false)
        verify { view.setVisibility(View.GONE) }
    }

    @Test
    fun isNetworkMissedTest() {
        isNetworkMissed(view, true)
        verify { view.setVisibility(View.VISIBLE) }

        isNetworkMissed(view, false)
        verify { view.setVisibility(View.GONE) }
    }

    @Test
    fun rotationTest() {
        val CIRCLE = 360
        val windDegree = 120F
        rotation(view, windDegree)
        verify { viewropertyAnimator.rotation(CIRCLE + windDegree) }
    }
}
