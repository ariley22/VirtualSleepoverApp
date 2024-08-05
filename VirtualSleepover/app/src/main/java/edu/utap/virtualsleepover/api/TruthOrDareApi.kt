package edu.utap.virtualsleepover.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class TruthOrDare(val question: String)

interface TruthOrDareApi {
    @GET("truth")
    suspend fun getTruthQuestion(): TruthOrDare

    @GET("dare")
    suspend fun getDare(): TruthOrDare

    @GET("wyr")
    suspend fun getWyrQuestion(): TruthOrDare

    companion object Factory {
        fun create(): TruthOrDareApi {
            val retrofit: Retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.truthordarebot.xyz/v1/")
                .build()
            return retrofit.create(TruthOrDareApi::class.java)
        }
    }

}