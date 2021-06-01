package com.coolcats.challengehiking.network

import com.coolcats.challengehiking.BuildConfig
import com.coolcats.challengehiking.mod.LocationResponse
import com.coolcats.challengehiking.util.Konstants.Companion.BASE_URL
import com.coolcats.challengehiking.util.Konstants.Companion.END_POINT

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class HikingNetwork {

    private val hikingRetrofit = createRetrofit().create(HikingEndPoint::class.java)

    suspend fun getAddressAsync(latlng: String): Deferred<LocationResponse> {
        return hikingRetrofit.getAddress(latlng, BuildConfig.API_KEY)
    }

    private fun createRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    interface HikingEndPoint {
        @GET(END_POINT)
        fun getAddress(
            @Query("latlng") latlng: String,
            @Query("key") key: String
        ): Deferred<LocationResponse>
    }

}