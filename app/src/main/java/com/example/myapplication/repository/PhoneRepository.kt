package com.example.myapplication.repository

import com.example.myapplication.BuildConfig
import com.example.myapplication.network.AbstractPhoneResponse
import com.example.myapplication.network.AbstractPhoneService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PhoneRepository {

    private val service: AbstractPhoneService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://phoneintelligence.abstractapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        service = retrofit.create(AbstractPhoneService::class.java)
    }

    suspend fun validatePhone(phone: String): AbstractPhoneResponse? {
        if (BuildConfig.ABSTRACT_API_KEY.isEmpty()) return null
        
        return try {
            val response = service.validatePhone(BuildConfig.ABSTRACT_API_KEY, phone)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            // Log error or handle specific exceptions like UnknownHostException (offline)
            null
        }
    }
}
