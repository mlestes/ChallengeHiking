package com.coolcats.challengehiking.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.HomeFragmentLayoutBinding
import com.coolcats.challengehiking.db.UserDB
import com.coolcats.challengehiking.db.UserDB.Companion.getUser
import com.coolcats.challengehiking.mod.User
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var binding: HomeFragmentLayoutBinding
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseAuth.getInstance().currentUser?.let {
            getUser(it)
            user = UserDB.user
        }

        logD(user.toString())
        binding.accountName.text = user.name
        binding.hikeCount.text = getString(R.string.hike_string, user.hikes)
        binding.challengeCount.text = getString(R.string.challenge_string, user.challenges)
        binding.milesCount.text = getString(R.string.miles_hiked_string, user.miles)
        binding.hoursCount.text = getString(R.string.hours_hiked_string, user.hours)
        if(user.lastHiked.isEmpty()) binding.lastHikeLocText.text = getString(R.string.empty_prev_loc)
        else binding.lastHikeLocText.text = getString(R.string.prev_hike_string, user.lastHiked)

    }

}