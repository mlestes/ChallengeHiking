package com.coolcats.challengehiking.view.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coolcats.challengehiking.databinding.SettingsFragmentLayoutBinding
import com.coolcats.challengehiking.util.Konstants.Companion.CHALLENGE_SETTING
import com.coolcats.challengehiking.util.Konstants.Companion.DISABLED
import com.coolcats.challengehiking.util.Konstants.Companion.ENABLED
import com.coolcats.challengehiking.util.Konstants.Companion.LOC_SETTING
import com.coolcats.challengehiking.util.Konstants.Companion.UNIT_SETTING
import com.coolcats.challengehiking.util.Logger.Companion.logD

class SettingsFragment : Fragment() {

    private lateinit var locPrefs: SharedPreferences
    private lateinit var chalPrefs: SharedPreferences
    private lateinit var unitPrefs: SharedPreferences
    private lateinit var binding: SettingsFragmentLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            locPrefs = it.getSharedPreferences(LOC_SETTING, Context.MODE_PRIVATE)
            chalPrefs = it.getSharedPreferences(CHALLENGE_SETTING, Context.MODE_PRIVATE)
            unitPrefs = it.getSharedPreferences(UNIT_SETTING, Context.MODE_PRIVATE)
        }

        val locEnabled = locPrefs.getInt(LOC_SETTING, DISABLED)
        val challengeEnabled = chalPrefs.getInt(CHALLENGE_SETTING, DISABLED)
        val useMetricUnits = unitPrefs.getInt(UNIT_SETTING, DISABLED)

        binding.locSwitch.isChecked = locEnabled == ENABLED
        binding.challengeSwitch.isChecked = challengeEnabled == ENABLED
        binding.unitSwitch.isChecked = useMetricUnits == ENABLED

        binding.challengeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                logD("Challenges enabled")
                chalPrefs.edit().putInt(CHALLENGE_SETTING, ENABLED).apply()
            } else {
                logD("Challenges disabled")
                chalPrefs.edit().putInt(CHALLENGE_SETTING, DISABLED).apply()
            }
        }

        binding.locSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                logD("Network Location Enabled")
                locPrefs.edit().putInt(LOC_SETTING, ENABLED).apply()
            } else {
                logD("Network Location Disabled")
                locPrefs.edit().putInt(LOC_SETTING, DISABLED).apply()
            }
        }

        binding.unitSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                logD("Using Metric Units")
                unitPrefs.edit().putInt(UNIT_SETTING, ENABLED).apply()
            } else {
                logD("Using Imperial Units")
                unitPrefs.edit().putInt(UNIT_SETTING, DISABLED).apply()
            }
        }

    }

}