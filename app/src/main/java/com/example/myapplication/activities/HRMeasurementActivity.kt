package com.example.myapplication.activities

import android.R
import android.app.Notification
import android.app.NotificationChannel
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
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


class HRMeasurementActivity() : ComponentActivity(), IBLeCharacteristicCallbacks {

    private var notificationManager: NotificationManager? = null

    private val NOTIFICATION_CHANNEL_ID = "BLE-Heart-Rate-Reader_PERMANENT"
    private var viewModel: HRMeasurementViewModel? = null

    var btGattService : BluetoothGattService? = null
    var btService: BluetoothLeService? = null
    var serviceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            //bound to service
            btService = (service as BluetoothLeService.LocalBinder).getService()
            btService?.startForeground(1, createNotification())
            onServiceBinded()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            //not bounded to service
            //btService?.disconnect()
            //btService = null
        }
    }


    private var device: BluetoothDevice? = null



    fun initializeViewModel(){
        viewModel = ViewModelProvider(this).get(HRMeasurementViewModel::class.java)
    }
    fun onServiceBinded(){
        btService?.registerActivityCharacteristicCallbacks(this@HRMeasurementActivity)
    }
    fun initObservers(){

    }
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Real time HR"
            val descriptionText = "Notification for keeping the app open so HR values come through"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                vibrationPattern = longArrayOf()
            }

            // Register the channel with the system
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("BLE Heart Rate Reader")
            .setContentText("Heart rate is being measured")
            .setSmallIcon(R.drawable.ic_notification_overlay)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
        return builder.build()
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
        btService!!.scope.launch {
            viewModel?.addMeasurement(BleUtil.intFromBytes(characteristic!!.value), device!!)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("BT", "killed measurement activity")
        //btService?.disconnect()
    }


}