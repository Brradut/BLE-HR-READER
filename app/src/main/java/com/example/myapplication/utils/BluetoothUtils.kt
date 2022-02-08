package com.example.myapplication.utils
//
//import android.app.Activity
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothGattCallback
//import android.bluetooth.BluetoothManager
//import android.bluetooth.le.*
//import android.content.Context
//import android.content.Intent
//import android.location.LocationManager
//import android.os.Build
//import android.os.Handler
//import android.provider.Settings
//import android.util.Log
//import android.widget.Toast
//import androidx.core.location.LocationManagerCompat
//import androidx.lifecycle.MutableLiveData
//
//
//class BluetoothUtils{
//    companion object {
//        private var REQUEST_ENABLE_BT  = 1;
//        private var REQUEST_ENABLE_LOCATION  = 2;
//        var btManager:BluetoothManager? = null
//        var btAdapter:BluetoothAdapter? = null
//        var btLeScanner:BluetoothLeScanner? = null
//        val scannedDevice = MutableLiveData<String>()
//
//        fun initBluetooth(context: Activity) {
//            Log.d("BT", "HELLO WE INITING STUFF HERE")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//               PermissionsUtils.requestBTPermission(context);
//            }
//
//            if(btManager == null) {
//                btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//                btAdapter = btManager!!.adapter
//                btLeScanner = btAdapter?.bluetoothLeScanner
//            }
//
//            if (btAdapter == null) {
//                Log.d("BT", "NU EXISTA BT :(")
//            }else if (btAdapter?.isEnabled == false) {
//                Log.d("BT", "DESCHIDE BT PLS?")
//                Toast.makeText(
//                    context,
//                    "Hello yes please open bluetooth thank you!!!",
//                    Toast.LENGTH_LONG
//                ).show()
//
//                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//            }
//
//        }
//
//        fun initLocation(context:Activity){
//            PermissionsUtils.requestLocationPermission(context)
//            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            if(!LocationManagerCompat.isLocationEnabled(lm)){
//                val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                context.startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION)
//            }
//
//        }
//
//        fun getDevices(){
//            Log.d("BT", "INCERCAM SA FURAM DEVICE HAHA")
//            val leScanCallback: ScanCallback = object : ScanCallback() {
//                override fun onScanResult(callbackType: Int, result: ScanResult?) {
//                    Log.d("BT", "${result?.rssi} ${result?.device?.name} ${result?.device?.address}")
//                    if(result != null)
//                        scannedDevice.postValue(result.device.address)
//                }
//             }
//
//             var scanning = false
//             val handler = Handler()
//             val SCAN_PERIOD: Long = 10000
//             val settings =  ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
//             val filters =  ArrayList<ScanFilter>()
//             filters.add(ScanFilter.Builder().build())
//                if (!scanning) { // Stops scanning after a pre-defined scan period.
//                    handler.postDelayed({
//                        scanning = false
//                        Log.d("BT", "nu mai scanam gata")
//                        btLeScanner?.stopScan(leScanCallback)
//                    }, SCAN_PERIOD)
//                    scanning = true
//                    Log.d("BT", "HAI CU SCANATUL")
//                    btLeScanner?.startScan(filters, settings, leScanCallback)
//                } else {
//                    Log.d("BT", "wow nu-ti place sa scanezi rude")
//                    scanning = false
//                    btLeScanner?.stopScan(leScanCallback)
//                }
//        }
//
//        fun startHr(context: Activity, device: BluetoothDevice, notifyCallback: BluetoothGattCallback){
//            val gatt = device.connectGatt(context, false, notifyCallback)
//            Log.d("BT", "${gatt.discoverServices()}")
//        }
//
//        fun intFromBytes(bytes: ByteArray): Int{
//            return bytes[bytes.size-1].toInt()
//        }
//
//    }
//
//
//}