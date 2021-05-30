package com.coolcats.challengehiking.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.SplashScreenBinding

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: SplashScreenBinding
    private lateinit var fadeIn: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.splashLogo.startAnimation(fadeIn)

        val intent = Intent(this, StartupActivity::class.java)
        Handler(mainLooper).postDelayed(
            {
                startActivity(intent.also {
                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                })
            },
        4000)

    }
}