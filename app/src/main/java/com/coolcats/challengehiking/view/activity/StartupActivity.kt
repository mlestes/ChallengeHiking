package com.coolcats.challengehiking.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.ActivityStartupBinding
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.coolcats.challengehiking.view.fragment.LoginFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class StartupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartupBinding
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fragmentContainer.visibility = View.VISIBLE
        if (checkLogin()) {
            logD("Logged in: Goto Starting Page")
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent.also {
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } else {
            logD("Not Logged in: Goto Login Page")
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun checkLogin(): Boolean {
        FirebaseAuth.getInstance().currentUser?.let {
            currentUser = it
        }
        return this::currentUser.isInitialized && currentUser.isEmailVerified
    }
}