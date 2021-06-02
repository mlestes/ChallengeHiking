package com.coolcats.challengehiking.view.fragment

import android.annotation.SuppressLint
import android.content.*
import android.content.Context.BIND_AUTO_CREATE
import android.content.Context.MODE_PRIVATE
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.HikingFragmentLayoutBinding
import com.coolcats.challengehiking.db.HikeDB.Companion.getHikeDB
import com.coolcats.challengehiking.db.UserDB
import com.coolcats.challengehiking.db.UserDB.Companion.getUserDB
import com.coolcats.challengehiking.mod.Hike
import com.coolcats.challengehiking.util.CHUtils.Companion.showError
import com.coolcats.challengehiking.util.Konstants.Companion.CHALLENGE_SETTING
import com.coolcats.challengehiking.util.Konstants.Companion.DEG_TO_METER
import com.coolcats.challengehiking.util.Konstants.Companion.DISABLED
import com.coolcats.challengehiking.util.Konstants.Companion.ENABLED
import com.coolcats.challengehiking.util.Konstants.Companion.FEET_TO_METER
import com.coolcats.challengehiking.util.Konstants.Companion.FEET_TO_MILE
import com.coolcats.challengehiking.util.Konstants.Companion.LOC_SETTING
import com.coolcats.challengehiking.util.Konstants.Companion.UNIT_SETTING
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.coolcats.challengehiking.util.Logger.Companion.logE
import com.coolcats.challengehiking.view.adapter.HikingLocationListener
import com.coolcats.challengehiking.view.adapter.HikingService
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
    private var timer: Timer = Timer("Hike Timer")
    private lateinit var prevLocation: Location
    private lateinit var currentLocation: Location
    private lateinit var startAddress: String
    private lateinit var endAddress: String
    private lateinit var time: String
    private lateinit var boundServiceIntent: Intent
    private var millis: Long = 0
    private var hikingService: HikingService? = null

    private val locListener = HikingLocationListener(this)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            logD("onServiceConnected Reached")
            hikingService = (service as HikingService.HikingBinder).getServiceClass()
            hikingService?.setupTracking(locListener)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            logE("Hiking Fragment: Error at ${name?.className} in serviceConnection")
            showError(binding.root, "An Error Occurred")
        }

    }

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

        boundServiceIntent = Intent(this.context, HikingService::class.java)
        context?.bindService(boundServiceIntent, serviceConnection, BIND_AUTO_CREATE)



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

        binding.challengeCountTxt.text = if (challengePrefs.getInt(
                CHALLENGE_SETTING,
                DISABLED
            ) == ENABLED
        ) getString(R.string.challenges_completed, challenges) else ""

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewModel.locationData.observe(viewLifecycleOwner, {
            binding.locTxt.text = getString(R.string.currently_located, it.formatted_address)
            if (!this::startAddress.isInitialized) startAddress = it.formatted_address
        })

        binding.startBtn.setOnClickListener {
            if (!doHike) {
                startTime = System.currentTimeMillis()
                context?.startForegroundService(boundServiceIntent)
                beginTrackingLocation()
                beginTimer()
                doHike = true
            }
        }

        binding.stopBtn.setOnClickListener {
            if (doHike) {
                stopTrackingLocation()
                context?.stopService(boundServiceIntent)
                context?.unbindService(serviceConnection)
                stopTimer()
                endAddress = viewModel.locationData.value?.formatted_address
                    ?: if (this::startAddress.isInitialized) startAddress else ""
                val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                logD(format.format(Date()))
                doHike = false
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
        timer = Timer("Hike Timer")
        timer.schedule(TimerHike(), 0, 500)
    }

    private fun stopTrackingLocation() {
        locationManager.removeUpdates(locListener)
    }

    private fun beginTrackingLocation() {
        registerListener()
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

    override fun provideLocation(location: Location) {
        if (this::currentLocation.isInitialized) prevLocation = currentLocation
        currentLocation = location
        if (this::prevLocation.isInitialized) calculateDistance(prevLocation, currentLocation)
        if ((distance > 0.0) && (distance % 1 == 0.0) && challengePrefs.getInt(
                CHALLENGE_SETTING,
                DISABLED
            ) == ENABLED
        ) doChallenge()
        getLocation(currentLocation)
    }

    private fun getLocation(location: Location) {
        viewModel.getAddress(location)
    }

    private fun calculateDistance(locA: Location, locB: Location) {
        val lat = locB.latitude - locA.latitude
        val long = locB.longitude - locA.longitude
        val unconvertedDistance = sqrt((lat * lat) + (long * long)) //in deg
        val metreDistance = unconvertedDistance * DEG_TO_METER //111,139 m/deg
        distance += if (unitPrefs.getInt(UNIT_SETTING, DISABLED) == ENABLED)
            (metreDistance / 1000.toDouble()) //convert to km
        else {
            val feetDistance = metreDistance * FEET_TO_METER //3.2804 ft/m (3280.4 ft/km)
            (feetDistance / FEET_TO_MILE.toDouble()) //5280 ft/mi
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