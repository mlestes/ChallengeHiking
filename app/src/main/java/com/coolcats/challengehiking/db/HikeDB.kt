package com.coolcats.challengehiking.db

import com.google.firebase.database.FirebaseDatabase

class HikeDB {

    companion object{
        fun getHikeDB() = FirebaseDatabase.getInstance().reference.child("Hikes")
    }
}