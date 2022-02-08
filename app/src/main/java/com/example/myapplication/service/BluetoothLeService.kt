package com.example.myapplication.service

import android.app.Activity
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.utils.BleUtil
import java.util.*

class BluetoothLeService : Service(){

    private val mBinder: IBinder =LocalBinder()
    private var bluetoothGatt: BluetoothGatt? = null
    private var device: MutableLiveData<BluetoothDevice>? = null
    private var scannedDevices: MutableLiveData<List<BluetoothDevice>>? = null
    private var scanResult: MutableLiveData<ScanResult>? = null
    var characteristicCallback: IBLeCharacteristicCallbacks? = null

    override fun onCreate() {
        super.onCreate()
    }

    fun scanForDevices(enable: Boolean) {
        Log.d("BT", "BLE Scan for devices: " + if (enable) "activated" else "deactivated")
        val callback = object: ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                //Log.d("BT","Got result ${result.device.address}")
                scanResult?.postValue(result)
            }
            override fun onScanFailed(errorCode: Int) {
                Log.d("BT", "what the fuck happened $errorCode")
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                Log.d("BT", "IS MULTE FOR SOME REASON")
            }
        }
        if (enable == true) {
            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            val filters = ArrayList<ScanFilter>()
            filters.add(ScanFilter.Builder().build())
            BleUtil.bleScanner?.startScan(filters, scanSettings, callback)
        } else {
            Log.d("BT", "No more scan")
            BleUtil.bleScanner?.stopScan(callback)
        }
    }

    fun registerActivityCharacteristicCallbacks(activity: Activity) {
        characteristicCallback = activity as IBLeCharacteristicCallbacks
    }

    private val gatCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(
                    "BT",
                    "onConnectionStateChange: Operation was successful for device " + gatt.device.address
                )
            } else {
                Log.d("BT", "onConnectionStateChange: Operation failed " + gatt.device.address)
            }
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(
                        "BT",
                        "onConnectionStateChange: Connected to GATT server of device " + gatt.device.address
                    )
                    Log.d(
                        "BT",
                        "onConnectionStateChange: Attempting to start service discovery:" + gatt.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> Log.d(
                    "BT",
                    "onConnectionStateChange: Disconnected from GATT server of device " + gatt.device.address
                )
                BluetoothProfile.STATE_CONNECTING -> Log.d(
                    "BT",
                    "onConnectionStateChange: Connecting to GATT server of device " + gatt.device.address
                )
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d("BT", "got the services boys")
            if(status== BluetoothGatt.GATT_SUCCESS){
                Log.d("BT", "AVEM SERVICII:D")
                val hrService = gatt?.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"))
                val hrCharacteristic = hrService?.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"))
                if(hrCharacteristic != null)
                    Log.d("BT", "AVEM INIMA")
                setCharacteristicNotification(hrCharacteristic!!, true)
            }else{
                Log.d("BT", "N-avem servicii:(")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristicCallback != null) {
                Log.d("BT","onCharacteristicChanged: Received notificaiton from characteristic " + characteristic.uuid + " of device " + gatt.device.address)
                characteristicCallback?.onCharacteristicNotify(characteristic)
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (scannedDevices == null) {
            scannedDevices = MutableLiveData<List<BluetoothDevice>>(emptyList())
        }
        if (device == null) device = MutableLiveData<BluetoothDevice>()
        if (scanResult == null) scanResult = MutableLiveData<ScanResult>()
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (bluetoothGatt != null) {
            bluetoothGatt!!.close()
            bluetoothGatt = null
        }
        return super.onUnbind(intent)
    }

    fun disconnect() {
        if (bluetoothGatt == null) {
            return
        }
        Log.d("BT", "disconnect: Disconnecting from device " + bluetoothGatt!!.getDevice().getAddress()
        )
        bluetoothGatt!!.disconnect()
        bluetoothGatt = null
    }

    fun connect(device: BluetoothDevice?): Boolean {
        this.device?.postValue(device)
            Log.d("BT","connect: Trying to connect to device  ${device?.address}")
            if (this.device?.getValue() != null && device?.address.equals(this.device?.getValue()!!.address) && bluetoothGatt != null) {
                Log.d("BT", "Trying to use an existing mBluetoothGatt for connection.")
                if (bluetoothGatt?.connect() == true) {
                    return true
                }
            }
            bluetoothGatt = device?.connectGatt(this, true, gatCallback)
            Log.d("BT","Trying to create a new connection to device with address " + device?.address)
        return true
    }

    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        if (BleUtil.btAdapter == null || bluetoothGatt == null) {
            Log.d("BT","setCharacteristicNotification failed: BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt?.setCharacteristicNotification(characteristic, enabled)
        val uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val descriptor = characteristic.getDescriptor(uuid)
        descriptor.value =
            if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        bluetoothGatt?.writeDescriptor(descriptor)
        Log.d("BT", "setCharacteristicNotification: " + enabled + " for characteristic (UUID): " + characteristic.uuid.toString()
        )
    }

    fun getScanResult(): LiveData<ScanResult?>? {
        return scanResult
    }

    fun getBluetoothDevice(): LiveData<BluetoothDevice?>? {
        return device
    }
}