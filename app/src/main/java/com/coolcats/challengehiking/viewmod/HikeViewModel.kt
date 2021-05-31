package com.coolcats.challengehiking.viewmod

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coolcats.challengehiking.mod.User
import com.coolcats.challengehiking.util.CHStatus

class HikeViewModel : ViewModel() {

    val statusData : MutableLiveData<CHStatus> = MutableLiveData()

}