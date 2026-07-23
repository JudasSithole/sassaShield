package com.example.myapplication.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AbstractPhoneService {
    @GET("v1/")
    suspend fun validatePhone(
        @Query("api_key") apiKey: String,
        @Query("phone") phone: String
    ): Response<AbstractPhoneResponse>
}
