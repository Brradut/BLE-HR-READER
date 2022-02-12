package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.model.HrEntry
import com.example.myapplication.service.HrRESTService
import com.example.myapplication.service.ServiceBuilder
import retrofit2.Response
import javax.inject.Inject


class HrEntryRepository {
    companion object {
    val service =ServiceBuilder.create()
    suspend fun addHrEntry(hr_entry: HrEntry){
        try {
            Log.d("BT", "incercam sa adaugam pe server")
            service.addHrEntry(hr_entry)
        }catch (ex: Exception){
            Log.d("BT", "ori n-avem net ori nu-i serverul pornit haha xd e ok punem in sqlite mai tarziu")

        }

    }
}
}