package com.coolcats.challengehiking.db

import android.util.Log
import com.coolcats.challengehiking.mod.User
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.coolcats.challengehiking.util.Logger.Companion.logE
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class UserDB {

    companion object {
        var user = User()
        fun getUserDB() = FirebaseDatabase.getInstance().reference.child("Users")
        fun getUser(currentUser: FirebaseUser) {
            getUserDB().get().addOnSuccessListener { snapshot ->
                    snapshot.children.forEach{ snap ->
                        snap.getValue(User::class.java)?.let {
                            user = it
                            if(user.name == currentUser.email) {
                                logD("Matched ${user.name} & ${currentUser.email}")
                                return@addOnSuccessListener
                            }
                        }
                        logD("User name current == ${user.name}")

                    }
                }.addOnFailureListener {
                    logE(it.localizedMessage)
                }
            logD("return ${user.name}")
        }
    }
}