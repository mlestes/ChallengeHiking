package com.coolcats.challengehiking.viewmod

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coolcats.challengehiking.mod.Result
import com.coolcats.challengehiking.network.HikingNetwork
import com.coolcats.challengehiking.util.CHStatus
import com.coolcats.challengehiking.util.Logger.Companion.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    val statusData: MutableLiveData<CHStatus> = MutableLiveData()
    val timerData: MutableLiveData<Long> = MutableLiveData()
    val locationData: MutableLiveData<Result> = MutableLiveData()

    private val appNetwork = HikingNetwork()
    private var job : Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getAddress(location: Location) {
        statusData.value = CHStatus.LOADING
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = appNetwork.getAddressAsync(location.toFormatString()).await()
                locationData.postValue(result.results[0])
                statusData.postValue(CHStatus.SUCCESS)

            } catch (e: Exception) {
                logE(e.localizedMessage)
                statusData.value = CHStatus.ERROR
            }
        }
    }

}

private fun Location.toFormatString(): String = "${this.latitude},${this.longitude}"
