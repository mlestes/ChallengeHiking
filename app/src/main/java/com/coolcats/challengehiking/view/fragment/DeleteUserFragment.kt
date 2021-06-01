package com.coolcats.challengehiking.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coolcats.challengehiking.databinding.DeleteUserFragmentBinding
import com.coolcats.challengehiking.db.HikeDB.Companion.getHikeDB
import com.coolcats.challengehiking.db.UserDB.Companion.getUser
import com.coolcats.challengehiking.db.UserDB.Companion.getUserDB
import com.coolcats.challengehiking.db.UserDB.Companion.user
import com.coolcats.challengehiking.util.CHUtils.Companion.showError
import com.coolcats.challengehiking.util.CHUtils.Companion.showMsg
import com.coolcats.challengehiking.view.activity.StartupActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class DeleteUserFragment : Fragment() {

    private lateinit var binding: DeleteUserFragmentBinding
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DeleteUserFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser?.let { getUser(it) }

        binding.deleteSubmitBtn.setOnClickListener {
            val pwd = binding.pwdDeleteInput.text.toString()
            val email = currentUser?.email ?: ""
            if (pwd.isEmpty()){
                showError(binding.root, "Password must not be empty")
                return@setOnClickListener
            }
            val credential = EmailAuthProvider.getCredential(email, pwd)
            currentUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser.delete().addOnCompleteListener {
                        if (it.isSuccessful) {
                            getUserDB().child(user.id).removeValue()
                            getHikeDB().child(user.id).removeValue()
                            showMsg(binding.root, "Account Deleted!")
                            val intent =
                                Intent(requireContext(), StartupActivity::class.java).also { i ->
                                    i.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            startActivity(intent)
                        } else {
                            showError(
                                binding.root,
                                it.exception?.localizedMessage ?: "An Error Occurred"
                            )
                        }
                    }
                } else
                    showError(binding.root, task.exception?.localizedMessage ?: "An Error Occurred")
            }
        }

    }
}