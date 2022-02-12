package com.example.myapplication.service

import com.example.myapplication.model.HrEntry
import com.example.myapplication.utils.PropertyUtils
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface HrRESTService {
    @POST("hr_entry")
    suspend fun addHrEntry(@Body hr_entry: HrEntry):Response<Unit>
}

class ServiceBuilder{
    companion object{
    fun create():HrRESTService{
        return Retrofit.Builder().baseUrl(PropertyUtils.getRESTEndpoint()).addConverterFactory(
            GsonConverterFactory.create()).build().create(HrRESTService::class.java)
    }
    }
}