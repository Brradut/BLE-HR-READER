package com.example.myapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HRMeasurementViewModel : ViewModel() {
    private val _HRMeasurements: MutableLiveData<MutableList<Int>> = MutableLiveData(mutableListOf())
    val HRMeasurements: LiveData<MutableList<Int>> = _HRMeasurements

    fun addMeasurement(ms: Int){
        Log.d("BT", "HR ADAUGAT WOOO INCREDIBIL")
        val mss = _HRMeasurements.value
        val new_mss = mss?.map { x->x } as MutableList<Int>
        new_mss.add(ms)
        _HRMeasurements.postValue(new_mss)
    }


}