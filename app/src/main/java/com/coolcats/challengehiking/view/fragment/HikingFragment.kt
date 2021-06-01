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
import com.coolcats.challengehiking.db.HikeDB.Companion.getHikeDB
import com.coolcats.challengehiking.db.UserDB
import com.coolcats.challengehiking.db.UserDB.Companion.getUserDB
import com.coolcats.challengehiking.mod.Hike
import com.coolcats.challengehiking.util.Konstants.Companion.CHALLENGE_SETTING
import com.coolcats.challengehiking.util.Konstants.Companion.DEG_TO_METRE
import com.coolcats.challengehiking.util.Konstants.Companion.DISABLED
import com.coolcats.challengehiking.util.Konstants.Companion.ENABLED
import com.coolcats.challengehiking.util.Konstants.Companion.FEET_TO_METRE
import com.coolcats.challengehiking.util.Konstants.Companion.FEET_TO_MILE
import com.coolcats.challengehiking.util.Konstants.Companion.LOC_SETTING
import com.coolcats.challengehiking.util.Konstants.Companion.REQUEST_CODE
import com.coolcats.challengehiking.util.Konstants.Companion.UNIT_SETTING
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.coolcats.challengehiking.view.adapter.HikingLocationListener
import com.coolcats.challengehiking.viewmod.AppViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class HikingFragment : Fragment(), HikingLocationListener.HikingLocationDelegate {

    private val viewModel: AppViewModel by activityViewModels()

    private lateinit var binding: HikingFragmentLayoutBinding
    private lateinit var locPrefs: SharedPreferences
    private lateinit var challengePrefs: SharedPreferences
    private lateinit var unitPrefs: SharedPreferences
    private lateinit var locationManager: LocationManager
    private lateinit var timer: Timer
    private lateinit var prevLocation: Location
    private lateinit var currentLocation: Location
    private lateinit var startAddress: String
    private lateinit var endAddress: String
    private lateinit var time: String
    private var millis: Long = 0

    private val locListener = HikingLocationListener(this)

    private var doHike = false
    private var startTime: Long = 0L
    private var distance = 0.0
    private var challenges = 0

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
            unitPrefs = it.getSharedPreferences(UNIT_SETTING, MODE_PRIVATE)
        }

        binding.timerTxt.text = getString(R.string.timer_format_txt, 0, 0, 0)
        binding.locTxt.text = getString(R.string.currently_located, "")

        viewModel.timerData.observe(viewLifecycleOwner, {
            millis = if (startTime > 0) it - startTime else 0
            var secs = (millis / 1000).toInt()
            var mins = secs / 60
            val hrs = mins / 60
            secs %= 60
            mins %= 60
            binding.timerTxt.text = getString(R.string.timer_format_txt, hrs, mins, secs)
            time = getString(R.string.timer_format_txt, hrs, mins, secs)
        })

        binding.challengeCountTxt.text = getString(R.string.challenges_completed, challenges)

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewModel.locationData.observe(viewLifecycleOwner, {
            binding.locTxt.text = getString(R.string.currently_located, it.formatted_address)
            if (!this::startAddress.isInitialized) startAddress = it.formatted_address
        })

        binding.startBtn.setOnClickListener {
            if (!doHike) {
                startTime = System.currentTimeMillis()
                beginTrackingLocation()
                beginTimer()
                doHike = true
            }
        }

        binding.stopBtn.setOnClickListener {
            if (doHike) {
                stopTrackingLocation()
                stopTimer()
                endAddress = viewModel.locationData.value?.formatted_address
                    ?: if (this::startAddress.isInitialized) startAddress else ""
                val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                logD(format.format(Date()))
                doHike = false
                //TODO: submit hike to DB
                val key = getHikeDB().child(UserDB.user.id).push().key ?: ""
                val uom = if (unitPrefs.getInt(UNIT_SETTING, DISABLED) == ENABLED) "km" else "miles"
                val hike = Hike(
                    key,
                    startAddress,
                    endAddress,
                    challenges,
                    time,
                    distance,
                    uom,
                    format.format(Date())
                )
                getHikeDB().child(UserDB.user.id).child(key).setValue(hike)

                val user = UserDB.user
                user.lastHiked = format.format(Date())
                user.hours += millis
                user.miles += distance.toFloat()
                user.hikes++
                user.challenges += challenges
                getUserDB().child(user.id).setValue(user)

                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                    .replace(R.id.main_frame, HomeFragment())
                    .commit()
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
                1F,
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
                            .setMessage("Location Permission is required for this app to function! Uninstall if permissions cannot be granted.")
                            .setPositiveButton("Open Settings") { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data =
                                    Uri.fromParts("package", requireActivity().packageName, null)
                                startActivity(intent)
                            }.create().show()
                    }
                }

    }

    override fun provideLocation(location: Location) {
        if (this::currentLocation.isInitialized) prevLocation = currentLocation
        currentLocation = location
        if (this::prevLocation.isInitialized) calculateDistance(prevLocation, currentLocation)
        if ((distance > 0.0) && (distance % 1 == 0.0)) doChallenge()
        getLocation(currentLocation)
    }

    private fun getLocation(location: Location) {
        viewModel.getAddress(location)
    }

    private fun calculateDistance(locA: Location, locB: Location) {
        val lat = locB.latitude - locA.latitude
        val long = locB.longitude - locA.longitude
        val unconvertedDistance = sqrt((lat * lat) + (long * long)) //in deg
        val metreDistance = unconvertedDistance * DEG_TO_METRE //convert to m
        distance += if (unitPrefs.getInt(UNIT_SETTING, DISABLED) == ENABLED)
            (metreDistance / 1000.toDouble()) //convert to km
        else {
            val feetDistance = metreDistance * FEET_TO_METRE //3.2804 ft/m (3280.4 ft/km)
            (feetDistance / FEET_TO_MILE.toDouble()) //111,139 m/deg.toDouble())  //5280 ft/mi
        }
        val uom = if (unitPrefs.getInt(UNIT_SETTING, DISABLED) == ENABLED) "km" else "miles"
        binding.distanceTxt.text = getString(R.string.distance_txt, distance, uom)
        logD("Distance: $distance $uom")
    }

    private fun doChallenge() {
        //do the challenge
        challenges++
    }

}