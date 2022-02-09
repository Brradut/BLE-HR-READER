package com.example.myapplication.model

import android.bluetooth.BluetoothDevice

class DeviceItem(val device:BluetoothDevice, val isConnectable:Boolean)  {

    override fun equals(other: Any?): Boolean {
        if (other?.javaClass == DeviceItem::class.java)
            return device.equals((other as DeviceItem).device)
        else
            return super.equals(other)
    }
}