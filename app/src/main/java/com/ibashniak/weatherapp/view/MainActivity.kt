package com.ibashniak.weatherapp.view

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.coroutineScope
import com.ibashniak.weatherapp.R
import com.ibashniak.weatherapp.data.Repository
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
import com.ibashniak.weatherapp.location.LocationProvider
import com.ibashniak.weatherapp.location.PermissionChecker
import com.ibashniak.weatherapp.ui.Animator
import com.ibashniak.weatherapp.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class MainActivity : AppCompatActivity(), KoinComponent {

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 222
    }

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var animator: Animator
    private lateinit var locationProvider: LocationProvider
    private val coroutineScope = lifecycle.coroutineScope

    private lateinit var activityMainBinding: ActivityMainBinding
    private val repo: Repository by inject()
    private lateinit var weatherViewModel: WeatherViewModel
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted)
                coroutineScope.launch {
                    PermissionChecker.checkPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                } else {
                weatherViewModel.checkNetwork()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationProvider = LocationProvider(this, coroutineScope)
        weatherViewModel =
            WeatherViewModel(applicationContext, coroutineScope, locationProvider, repo)
        connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityMainBinding.lifecycleOwner = this
        activityMainBinding.viewModel = weatherViewModel
        animator = Animator(this, activityMainBinding.ivWindDirection, repo.currentWeather)

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    override fun onResume() {
        super.onResume()
        weatherViewModel.checkNetwork()
    }

    override fun onPause() {
        Timber.d("onPause")
        super.onPause()
        locationProvider.stopLocationUpdates()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}
