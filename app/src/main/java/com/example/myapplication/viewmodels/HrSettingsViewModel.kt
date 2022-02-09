package com.example.myapplication.viewmodels

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.DeviceItem

class HrSettingsViewModel: ViewModel() {

    val _scannedDevices : MutableLiveData<MutableList<DeviceItem>> = MutableLiveData<MutableList<DeviceItem>>(mutableListOf())
    val scannedDevices : LiveData<MutableList<DeviceItem>> = _scannedDevices

    fun clearDeviceList(){
        _scannedDevices.postValue(mutableListOf())
    }

    fun addDevice(device: DeviceItem) {
        Log.d("BT", "AM AJUNS AICI SALUT SUNTEM LA ADD DEVICE")
        val devs = _scannedDevices.value
        if(!devs?.contains(device)!!){
            Log.d("BT", "L-AM SI ADAUGAT ITI VINE SA CREZI??")
            devs?.add(device)
            _scannedDevices.postValue(devs.map { x -> x } as MutableList<DeviceItem>)
        }
    }

}