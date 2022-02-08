package com.example.myapplication.activities

import android.bluetooth.*
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.service.BluetoothLeService
import com.example.myapplication.service.IBLeCharacteristicCallbacks
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.BleUtil
import com.example.myapplication.viewmodels.HRMeasurementViewModel
import java.lang.Boolean
import java.util.*

class HRMeasurementActivity : ComponentActivity(), IBLeCharacteristicCallbacks {

    var btGattService : BluetoothGattService? = null
    var btService: BluetoothLeService? = null
    var serviceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            //bound to service
            btService = (service as BluetoothLeService.LocalBinder).getService()
            onServiceBinded()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            //not bounded to service
            btService?.disconnect()
            btService = null
        }
    }


    private var device: BluetoothDevice? = null

    var viewModel: HRMeasurementViewModel? = null

    val gattCallback  = object : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d("BT", "got the services boys")
            if(status== BluetoothGatt.GATT_SUCCESS){
                Log.d("BT", "AVEM SERVICII:D")
                val hrService = gatt?.getService(UUID.fromString("0000180D-1000-8000-00805f9b34fb"))
                val hrCharacteristic = hrService?.getCharacteristic(UUID.fromString("00002A37-1000-8000-00805f9b34fb"))
                gatt?.setCharacteristicNotification(hrCharacteristic, true)
            }else{
                Log.d("BT", "N-avem servicii:(")
            }
        }
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Log.d("BT", "CHARACTERSTIC CHANGED :O")
            if(characteristic?.uuid?.compareTo(UUID.fromString("00002A37-1000-8000-00805f9b34fb")) ==0){
                viewModel?.addMeasurement(BleUtil.intFromBytes(characteristic.value))
            }
        }
    }
    fun initializeViewModel(){
        viewModel = ViewModelProvider(this).get(HRMeasurementViewModel::class.java)
    }
    fun onServiceBinded(){
        btService?.registerActivityCharacteristicCallbacks(this@HRMeasurementActivity)
    }
    fun initObservers(){

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
        val serviceIntent = Intent(applicationContext, BluetoothLeService::class.java)
        val success = applicationContext.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        Log.d("BT", "bindService returned: " + Boolean.toString(success))

        device = BleUtil.btAdapter?.getRemoteDevice(intent.getStringExtra("device"))
        initObservers()

        setContent {
            MyApplicationTheme() {
                androidx.compose.material.Surface {
                    val measurementList by viewModel?.HRMeasurements!!.observeAsState(mutableListOf())
                    LazyColumn(){
                        items(measurementList){
                            item -> Text(text="$item")
                        }
                    }
                }
            }
        }
    }

    override fun onCharacteristicRead(characteristic: BluetoothGattCharacteristic?) {
        TODO("Not yet implemented")
    }

    override fun onCharacteristicWrite(characteristic: BluetoothGattCharacteristic?) {
        TODO("Not yet implemented")
    }

    override fun onCharacteristicNotify(characteristic: BluetoothGattCharacteristic?) {
        viewModel?.addMeasurement(BleUtil.intFromBytes(characteristic!!.value))
    }


}