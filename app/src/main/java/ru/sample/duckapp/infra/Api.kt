package ru.sample.duckapp.infra

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.sample.duckapp.data.DucksApi

object Api {
    private const val BASE_URL = "https://random-d.uk/api/v2/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
    }

    val ducksApi by lazy {
        retrofit.create(DucksApi::class.java)
    }
}