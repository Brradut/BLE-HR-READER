package com.example.myapplication.utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast

object BleUtil {
    const val REQUEST_ENABLE_BT_RESULT = 313
    var btAdapter: BluetoothAdapter? = null
    var bleScanner: BluetoothLeScanner? = null


    fun checkIsBluetoothEnabled(ctx: Activity) {
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        bleScanner = btAdapter?.getBluetoothLeScanner()
        if (btAdapter == null || !btAdapter!!.isEnabled) {
            Log.d("BT", "checkIsBluetoothEnabled: The Bluetooth adapter is not enabled. Sending request to enable the adapter")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ctx.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_RESULT)
        }
    }

    fun checkBleAvailability(ctx: Activity) {
        if (!ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d("BT", "checkBleAvailability: Device does not support bluetooth low energy. System Feature " + PackageManager.FEATURE_BLUETOOTH_LE + " is missing.")
            Toast.makeText(ctx, "Seems like your device doesn\\'t support BLE!", Toast.LENGTH_SHORT).show()
        }
    }
    fun intFromBytes(bytes: ByteArray): Int{
            return bytes[bytes.size-1].toInt()
        }

}