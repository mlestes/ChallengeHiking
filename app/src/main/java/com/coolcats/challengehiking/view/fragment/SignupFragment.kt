package com.coolcats.challengehiking.view.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coolcats.challengehiking.databinding.SignupFragmentLayoutBinding
import com.coolcats.challengehiking.db.UserDB.Companion.getUserDB
import com.coolcats.challengehiking.mod.User
import com.coolcats.challengehiking.util.CHUtils.Companion.showError
import com.coolcats.challengehiking.util.CHUtils.Companion.showMsg
import com.coolcats.challengehiking.util.Konstants.Companion.USER_KEY
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment() {

    private lateinit var binding: SignupFragmentLayoutBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SignupFragmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            preferences = it.getSharedPreferences(USER_KEY, Context.MODE_PRIVATE)
        }

        binding.signupSubmitBtn.setOnClickListener {
            val emailInput = binding.signupEmailInput.text.toString().trim()
            val emailConf = binding.signupEmailConfirmInput.text.toString().trim()
            val pwdInput = binding.pwdSignupInput.text.toString()
            val pwdConf = binding.pwdSignupConfirmInput.text.toString()
            if (checkInput(emailInput, emailConf, pwdInput, pwdConf)) {
                logD("Attempt Signup")
                performSignUp(emailInput, pwdInput)
            } else {
                showError(
                    binding.root,
                    "Check input. Emails must match, passwords must match, and cannot be empty"
                )
            }
        }

    }

    private fun performSignUp(emailInput: String, pwdInput: String) {

        val database = getUserDB()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailInput, pwdInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val key = database.push().key ?: ""
                    val user = User(key, emailInput, 0.0F, 0.0F, 0, 0, "")
                    preferences.edit().putString(USER_KEY, key).apply()
                    database.child(key).setValue(user)
                    FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                    showMsg(binding.root, "Check Email for Verification Link!")
                    val count = requireActivity().supportFragmentManager.backStackEntryCount
                    for (i in 0..count) requireActivity().supportFragmentManager.popBackStack()
                }
            }
    }

    private fun checkInput(
        emailInput: String,
        emailConf: String,
        pwdInput: String,
        pwdConf: String
    ): Boolean =
        emailInput == emailConf && pwdInput == pwdConf && emailInput.isNotEmpty() && pwdInput.isNotEmpty()

}