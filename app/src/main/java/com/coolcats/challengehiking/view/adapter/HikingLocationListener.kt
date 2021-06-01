package com.coolcats.challengehiking.view.adapter

import android.location.Location
import android.location.LocationListener

class HikingLocationListener(private val delegate: HikingLocationDelegate) : LocationListener {

    interface HikingLocationDelegate {
        fun provideLocation(location: Location)
    }

    override fun onLocationChanged(location: Location) {
        delegate.provideLocation(location)
    }
}