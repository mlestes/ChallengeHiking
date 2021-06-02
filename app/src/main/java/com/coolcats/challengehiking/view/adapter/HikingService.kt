package com.coolcats.challengehiking.view.adapter

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.util.Konstants
import com.coolcats.challengehiking.util.Konstants.Companion.CHANNEL_ID
import com.coolcats.challengehiking.util.Konstants.Companion.LOC_SETTING
import com.coolcats.challengehiking.util.Konstants.Companion.REQUEST_CODE
import com.coolcats.challengehiking.util.Logger.Companion.logD
import kotlin.math.log

class HikingService : Service() {

    private lateinit var locationListener: HikingLocationListener
    private lateinit var locationManager: LocationManager
    private lateinit var locPrefs: SharedPreferences

    inner class HikingBinder : Binder() {
        fun getServiceClass(): HikingService {
            return this@HikingService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return HikingBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopTracking()
        return super.onUnbind(intent)
    }

    fun setupTracking(listener: HikingLocationListener) {
        locationListener = listener
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logD("onStartCommand")
        beginTracking()
        return START_NOT_STICKY
    }

    override fun onCreate() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locPrefs = getSharedPreferences(LOC_SETTING, MODE_PRIVATE)
        super.onCreate()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        logD("onTaskRemoved")
        stopTracking()
        super.onTaskRemoved(rootIntent)
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

        logD("HikingService: creating notification")
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    packageName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Tracing location...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .build()

        startForeground(REQUEST_CODE, notification)
        val nManager = NotificationManagerCompat.from(this)
        nManager.notify(REQUEST_CODE, notification)
    }

    fun stopTracking() {
        logD("HikingService: stopTracking")
        locationManager.removeUpdates(locationListener)
    }
}