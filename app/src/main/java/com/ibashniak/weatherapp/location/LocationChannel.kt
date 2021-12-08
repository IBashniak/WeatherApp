package com.ibashniak.weatherapp.location

import android.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class LocationChannel(private val scope: CoroutineScope) {
    private val channel = Channel<Location>()

    fun send(data : Location){
        scope.launch {
            channel.send(data)
        }
    }

    suspend fun getLocation() : Location
    {
        return channel.receive()
    }
}