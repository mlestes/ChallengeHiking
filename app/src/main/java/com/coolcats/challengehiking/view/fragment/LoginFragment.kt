package com.coolcats.challengehiking.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.LoginFragmentLayoutBinding
import com.coolcats.challengehiking.util.CHUtils.Companion.showError
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.coolcats.challengehiking.util.Logger.Companion.logE
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginSubmitBtn.setOnClickListener {
            val email = binding.emailLoginInput.text.toString().trim()
            val pwd = binding.pwdLoginInput.text.toString()
            if(checkInput(email, pwd)) {
                logD("Logging In...")
                performLogin(email, pwd)
            }
            else {
                logE("Invalid Input: empty email/pwd")
                showError(binding.root, "Email/Password must not be empty")
            }
        }

        binding.newUserBtn.setOnClickListener {
            val fragment = SignupFragment()
            logD("Going to SignUpFragment")
            requireActivity().supportFragmentManager.beginTransaction()
                .addToBackStack(fragment.tag)
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

    }

    private fun performLogin(email: String, pwd: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    if(FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
                        logD("Logged In Successful")
                        //go to start page
                    } else {
                        logD("Show Email Verification msm")
                        showError(binding.root, "User must verify Email first!")
                    }
                } else {
                    logE("Invalid Input: $email $pwd")
                    showError(binding.root, "${task.exception?.message}")
                }
            }
    }

    private fun checkInput(email: String, pwd: String): Boolean {
        return email.isNotEmpty() && pwd.isNotEmpty()
    }

}