package com.coolcats.challengehiking.db

import com.google.firebase.database.FirebaseDatabase

class UserDB {

    companion object {
        fun getUserDB() = FirebaseDatabase.getInstance().reference.child("Users")
    }
}