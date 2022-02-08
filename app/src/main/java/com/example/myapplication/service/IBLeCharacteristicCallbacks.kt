package com.example.myapplication.service

import android.bluetooth.BluetoothGattCharacteristic

interface IBLeCharacteristicCallbacks {
    fun onCharacteristicRead(characteristic: BluetoothGattCharacteristic?)
    fun onCharacteristicWrite(characteristic: BluetoothGattCharacteristic?)
    fun onCharacteristicNotify(characteristic: BluetoothGattCharacteristic?)
}
