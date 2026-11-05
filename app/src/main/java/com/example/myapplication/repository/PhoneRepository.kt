package com.example.myapplication.repository

import com.example.myapplication.BuildConfig
import com.example.myapplication.network.AbstractPhoneResponse
import com.example.myapplication.network.AbstractPhoneService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class PhoneValidationResult {
    data class Success(val response: AbstractPhoneResponse) : PhoneValidationResult()
    data class ApiError(val code: Int, val message: String?) : PhoneValidationResult()
    data class NetworkError(val exception: Exception) : PhoneValidationResult()
    object NoApiKey : PhoneValidationResult()
}

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

    suspend fun validatePhone(phone: String): PhoneValidationResult {
        if (BuildConfig.ABSTRACT_API_KEY.isEmpty()) return PhoneValidationResult.NoApiKey

        return try {
            val response: Response<AbstractPhoneResponse> = service.validatePhone(BuildConfig.ABSTRACT_API_KEY, phone)
            if (response.isSuccessful) {
                response.body()?.let {
                    PhoneValidationResult.Success(it)
                } ?: PhoneValidationResult.ApiError(response.code() ?: -1, "Empty response body")
            } else {
                PhoneValidationResult.ApiError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            PhoneValidationResult.NetworkError(e)
        }
    }
}
