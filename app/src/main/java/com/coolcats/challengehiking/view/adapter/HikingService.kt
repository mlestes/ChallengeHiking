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

    fun setupTracking(listener: HikingLocationListener) {
        locationListener = listener
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        beginTracking()
        return START_NOT_STICKY
    }

    override fun onCreate() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locPrefs = getSharedPreferences(LOC_SETTING, MODE_PRIVATE)
        super.onCreate()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopTracking()
        super.onTaskRemoved(rootIntent)
    }

    @SuppressLint("MissingPermission")
    private fun beginTracking() {
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

    private fun stopTracking() {
        locationManager.removeUpdates(locationListener)
    }
}