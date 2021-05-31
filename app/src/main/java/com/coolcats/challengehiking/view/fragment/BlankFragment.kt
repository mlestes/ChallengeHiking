package com.coolcats.challengehiking.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coolcats.challengehiking.databinding.BlankFragmentLayoutBinding

class BlankFragment : Fragment() {

    private lateinit var binding: BlankFragmentLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BlankFragmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }
}