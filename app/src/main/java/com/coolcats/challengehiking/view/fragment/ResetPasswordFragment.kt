package com.coolcats.challengehiking.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.ResetPasswordLayoutBinding
import com.coolcats.challengehiking.util.CHUtils.Companion.showError
import com.coolcats.challengehiking.util.CHUtils.Companion.showMsg
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordFragment: Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var binding: ResetPasswordLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ResetPasswordLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pwdSubmitBtn.setOnClickListener {
            val currentPwd = binding.currentPwdInput.text.toString()
            val email = currentUser?.email ?: ""
            val newPwd = binding.newPwdInput.text.toString()
            val confPwd = binding.resetPwdConf.text.toString()
            if (currentPwd.isEmpty() || newPwd.isEmpty() || confPwd.isEmpty()){
                showError(binding.root, "Fields must not be empty")
                return@setOnClickListener
            }
            val credential = EmailAuthProvider.getCredential(email, currentPwd)
            currentUser?.reauthenticate(credential)?.addOnCompleteListener {
                if (it.isCanceled) showError(
                    binding.root,
                    it.exception?.message ?: "An Error has occurred"
                )
                else {
                    if (newPwd == confPwd && newPwd.isNotEmpty()) {
                        currentUser?.updatePassword(newPwd).addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                showMsg(binding.root, "Password Reset!")
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .setCustomAnimations(
                                        android.R.anim.fade_in,
                                        android.R.anim.fade_out,
                                        android.R.anim.fade_in,
                                        android.R.anim.fade_out
                                    )
                                    .replace(R.id.main_frame, BlankFragment())
                                    .commit()
                            }
                            else task.exception?.let { err ->
                                showError(
                                    binding.root,
                                    err.localizedMessage
                                )
                            }
                        }
                    } else showError(binding.root, "Passwords must match and not be empty!")
                }
            }
        }
    }
}