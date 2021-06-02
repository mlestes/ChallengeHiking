package com.coolcats.challengehiking.view.adapter

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import com.coolcats.challengehiking.util.Konstants
import com.coolcats.challengehiking.util.Konstants.Companion.LOC_SETTING

class HikingService : Service() {

    private lateinit var hikingDelegate: HikingDelegate
    private lateinit var locationListener: HikingLocationListener
    private lateinit var locationManager: LocationManager
    private lateinit var locPrefs : SharedPreferences

    inner class HikingBinder : Binder(){
        fun getServiceClass() : HikingService {
            return this@HikingService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return HikingBinder()
    }

    interface HikingDelegate {
        fun trackLocation()
    }

    fun setupTracking(delegate: HikingDelegate, listener: HikingLocationListener) {
        hikingDelegate = delegate
        locationListener = listener
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locPrefs = getSharedPreferences(LOC_SETTING, MODE_PRIVATE)
        super.onCreate()
    }

    @SuppressLint("MissingPermission")
    fun beginTracking() {
        if (locPrefs.getInt(Konstants.LOC_SETTING, Konstants.DISABLED) == Konstants.ENABLED)
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                1F,
                locationListener
            )
        else
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1F,
                locationListener
            )
    }

    fun stopTracking() {
        locationManager.removeUpdates(locationListener)
    }
}