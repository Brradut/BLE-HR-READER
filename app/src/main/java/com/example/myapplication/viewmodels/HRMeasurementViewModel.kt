package com.example.myapplication.viewmodels

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.HrEntry
import com.example.myapplication.model.LoginToken
import com.example.myapplication.repository.HrEntryRepository
import com.example.myapplication.utils.PropertyUtils
import kotlinx.coroutines.launch
import javax.inject.Inject

class HRMeasurementViewModel: ViewModel() {
    private val _HRMeasurements: MutableLiveData<MutableList<Int>> = MutableLiveData(mutableListOf())
    val HRMeasurements: LiveData<MutableList<Int>> = _HRMeasurements

    fun addMeasurement(ms: Int, device: BluetoothDevice){
        Log.d("BT", "HR ADAUGAT WOOO INCREDIBIL")
        val mss = _HRMeasurements.value
        val new_mss = mss?.map { x->x } as MutableList<Int>
        new_mss.add(ms)
        _HRMeasurements.postValue(new_mss)
        viewModelScope.launch {
            HrEntryRepository.addHrEntry(HrEntry(ms, System.currentTimeMillis(), device.address, PropertyUtils.getLoginToken()))
        }
    }


}