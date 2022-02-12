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
import com.example.myapplication.BLECommands
import com.example.myapplication.utils.BleUtil
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class BluetoothLeService : Service(){

    private val mBinder: IBinder =LocalBinder()
    private var bluetoothGatt: BluetoothGatt? = null
    private var device: MutableLiveData<BluetoothDevice>? = null
    private var scannedDevices: MutableLiveData<List<BluetoothDevice>>? = null
    private var scanResult: MutableLiveData<ScanResult>? = null
    var characteristicCallback: IBLeCharacteristicCallbacks? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("BT", "started service")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BT", "killed service")
        disconnect()
    }

    fun scanForDevices(enable: Boolean) {
        Log.d("BT", "BLE Scan for devices: " + if (enable) "activated" else "deactivated")
        val callback = object: ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                //Log.d("BT","Got result ${result.device.address}")
                scanResult?.postValue(result)
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
               // for(i in gatt.services)
                 //   Log.d("BT", "AVEM AICI SERVICIUL ${i.uuid} SI CARACTERISTICILE ${i.characteristics.map { x->x.uuid.toString() + " " + x.properties + " " + x.permissions }.reduceOrNull({x, y->x + "\n" + y})}")
                val hrService = gatt?.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"))
                val mainService = gatt?.getService(UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb"))
                val controlCharacteristic = mainService?.getCharacteristic(UUID.fromString("00000003-0000-3512-2118-0009af100700"))
                val hrMeasurementCharacteristic = hrService?.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"))
                val hrControlCharacteristic = hrService?.getCharacteristic(UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb"))
                if(controlCharacteristic!= null){
                    thread(start = true){
                        writeCharacteristicLock.withLock {
                            BLECommands.enableHrConnection(gatt, controlCharacteristic)
                            writeCharacteristicCondition.await()
                        }
                    }
                }

                if(hrMeasurementCharacteristic != null && hrControlCharacteristic != null) {
                    Log.d("BT", "AVEM INIMA")
                    thread(start=true) {
                        if(device?.value?.name.equals("Mi Smart Band 5", ignoreCase = true)){
                            writeCharacteristicLock.withLock {
                                setCharacteristicNotification(hrMeasurementCharacteristic, true)
                                writeCharacteristicCondition.await()
                            }
                         }
                        BLECommands.setupHrMeasurement(gatt, hrControlCharacteristic, hrMeasurementCharacteristic)
                    }
                    }
            }else{
                Log.d("BT", "N-avem servicii:(")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            writeCharacteristicLock.withLock {
            if(descriptor != null)
                Log.d("BT", "WRITE DESCRIPTOR $status IDK WHAT THIS NUMBER MEANS ALSO THIS IS THE VALUE ${descriptor.value.map { x-> x.toString() }.reduce({x, y -> x + " " + y})} AND UUID ${descriptor.uuid}")
            else
                Log.d("BT", "cica am scris dar nu se vede idk")
            writeCharacteristicCondition.signal()
        }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if(characteristic != null)
                Log.d("BT", "READ CHARACTERISTIC $status IDK WHAT THIS NUMBER MEANS ALSO THIS IS THE VALUE ${characteristic.value.map { x-> x.toString() }.reduce({x, y -> x + " " + y})} AND UUID ${characteristic.uuid}")
            else
                Log.d("BT", "cica am scris dar nu se vede idk")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            writeCharacteristicLock.withLock {
                if (characteristic != null)
                    Log.d(
                        "BT",
                        "WRITE CHARACTERISTIC $status IDK WHAT THIS NUMBER MEANS ALSO THIS IS THE VALUE ${
                            characteristic.value.map { x -> x.toString() }
                                .reduce({ x, y -> x + " " + y })
                        } AND UUID ${characteristic.uuid}"
                    )
                else
                    Log.d("BT", "cica am citit dar nu se vede idk")
                writeCharacteristicCondition.signal()
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
        if(BleUtil.btAdapter == null || device == null) {
            Log.d(
                "BT",
                "Connect to GattServer failed. Bluetooth Adapter Sate: " + BleUtil.btAdapter.toString() + ", Device State: " + device
            )
            return false
        }else{
                Log.d("BT", "connect: Trying to connect to device  ${device?.address}")
                if (this.device?.getValue() != null && device?.address.equals(this.device?.getValue()!!.address) && bluetoothGatt != null) {
                    Log.d("BT", "Trying to use an existing mBluetoothGatt for connection.")
                    if (bluetoothGatt?.connect() == true) {
                        return true
                    }
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

    companion object{
        val writeCharacteristicLock = ReentrantLock()
        val writeCharacteristicCondition = writeCharacteristicLock.newCondition()
    }
}