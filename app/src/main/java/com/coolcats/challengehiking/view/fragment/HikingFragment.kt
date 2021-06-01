package com.coolcats.challengehiking.view.fragment

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.HikingFragmentLayoutBinding
import com.coolcats.challengehiking.util.Konstants.Companion.CHALLENGE_SETTING
import com.coolcats.challengehiking.util.Konstants.Companion.DISABLED
import com.coolcats.challengehiking.util.Konstants.Companion.ENABLED
import com.coolcats.challengehiking.util.Konstants.Companion.LOC_SETTING
import com.coolcats.challengehiking.util.Konstants.Companion.REQUEST_CODE
import com.coolcats.challengehiking.util.Konstants.Companion.TIMER
import com.coolcats.challengehiking.view.adapter.HikingLocationListener
import com.coolcats.challengehiking.viewmod.AppViewModel
import java.util.*

class HikingFragment : Fragment(), HikingLocationListener.HikingLocationDelegate {

    private val viewModel: AppViewModel by activityViewModels()

    private lateinit var binding: HikingFragmentLayoutBinding
    private lateinit var locPrefs: SharedPreferences
    private lateinit var challengePrefs: SharedPreferences
    private lateinit var locationManager: LocationManager
    private val locListener = HikingLocationListener(this)
    private lateinit var timer: Timer
    private var doHike = false
    private var startTime: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HikingFragmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            locPrefs = it.getSharedPreferences(LOC_SETTING, MODE_PRIVATE)
            challengePrefs = it.getSharedPreferences(CHALLENGE_SETTING, MODE_PRIVATE)
        }

        binding.timerTxt.text = getString(R.string.timer_format_txt, 0, 0, 0)
        binding.locTxt.text = getString(R.string.currently_located, "")

        viewModel.timerData.observe(viewLifecycleOwner, {
            var millis = if(startTime > 0) it - startTime else 0
            var secs = (millis / 1000).toInt()
            var mins = secs / 60
            var hrs = mins / 60
            secs %= 60
            mins %= 60
            binding.timerTxt.text = getString(R.string.timer_format_txt, hrs, mins, secs)
        })

        var challenges = 0
        binding.challengeCountTxt.text = getString(R.string.challenges_completed, challenges)

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewModel.locationData.observe(viewLifecycleOwner, {
            binding.locTxt.text = getString(R.string.currently_located, it.formatted_address)
        })

        binding.startBtn.setOnClickListener {
            if (doHike == false) {
                startTime = System.currentTimeMillis()

                beginTrackingLocation()
                beginTimer()
                doHike = true

            }
        }

        binding.stopBtn.setOnClickListener {
            if (doHike == true) {
                stopTrackingLocation()
                stopTimer()
                doHike = false
            }
        }

    }

    inner class TimerHike : TimerTask() {
        override fun run() {
            val millis = System.currentTimeMillis()
            viewModel.timerData.postValue(millis)
        }
    }

    private fun stopTimer() {
        timer.cancel()
    }

    private fun beginTimer() {
        timer = Timer()
        timer.schedule(TimerHike(), 0, 500)
    }



    private fun stopTrackingLocation() {
        locationManager.removeUpdates(locListener)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun beginTrackingLocation() {
        if (activity?.let {
                ActivityCompat.checkSelfPermission(
                    it.applicationContext,
                    ACCESS_FINE_LOCATION
                )
            } == PERMISSION_GRANTED)
            registerListener()
        else
            requestPermission()
    }

    @SuppressLint("MissingPermission")
    private fun registerListener() {
        if (locPrefs.getInt(LOC_SETTING, DISABLED) == ENABLED)
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                1F,
                locListener
            )
        else
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                locListener
            )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION),
            REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE)
            if (permissions[0] == ACCESS_FINE_LOCATION)
                if (grantResults[0] == PERMISSION_GRANTED)
                    registerListener()
                else {
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        requestPermission()
                    } else {
                        AlertDialog.Builder(
                            ContextThemeWrapper(
                                requireContext(),
                                R.style.ThemeOverlay_AppCompat
                            )
                        )
                            .setTitle("Permission Needed!")
                            .setMessage("Location Permission is required for this app to function! Unistall if permissions cannot be granted.")
                            .setPositiveButton("Open Settings") { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data =
                                    Uri.fromParts("package", requireActivity().packageName, null)
                                startActivity(intent)
                            }.create().show()
                    }
                }

    }

    override fun provideLocation(location: Location) = getLocation(location)

    private fun getLocation(location: Location) {
        viewModel.getAddress(location)
    }

}