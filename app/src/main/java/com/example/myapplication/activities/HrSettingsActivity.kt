package com.example.myapplication.activities

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.model.DeviceItem
import com.example.myapplication.service.BluetoothLeService
import com.example.myapplication.viewmodels.HrSettingsViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.BleUtil
import com.example.myapplication.utils.PermissionsUtils
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Boolean

class HrSettingsActivity : ComponentActivity() {

    private var btService: BluetoothLeService? = null
    private var viewModel:HrSettingsViewModel? = null

    fun initObservers(){
        btService?.getScanResult()?.observe(this,{
            result -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel?.addDevice(DeviceItem(result?.device!!, result?.isConnectable!!))
        }else{
            viewModel?.addDevice(DeviceItem(result?.device!!, false))
        }
        })
        viewModel?.scannedDevices?.observe(this, {
            deviceList -> Log.d("BT", "$deviceList")
        })
    }

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

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Log.d("BT", "onReceive: Bluetooth Adapter was turned off")
                    }
                    BluetoothAdapter.STATE_ON -> {
                        Log.d("BT", "onReceive: Bluetooth Adapter was turned on")
                        BleUtil.btAdapter = BluetoothAdapter.getDefaultAdapter()
                        BleUtil.bleScanner = BleUtil.btAdapter!!.getBluetoothLeScanner()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
        val serviceIntent = Intent(applicationContext, BluetoothLeService::class.java)
        val success =
            applicationContext.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        Log.d("BT", "bindService returned: " + Boolean.toString(success))

        PermissionsUtils.requestBTPermission(this)
        PermissionsUtils.requestLocationPermission(this)
        PermissionsUtils.requestReadStoragePermission(this)
        BleUtil.checkIsBluetoothEnabled(this)
        BleUtil.checkBleAvailability(this)

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mReceiver, filter)

        initObservers()

        initObservers()
        setContent {
            app()
        }
    }

    fun initializeViewModel() {
        viewModel = ViewModelProvider(this).get(HrSettingsViewModel::class.java)
    }

    fun onServiceBinded() {
        btService?.getScanResult()?.observe(this@HrSettingsActivity,
            Observer<ScanResult?> { scanResult -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                viewModel?.addDevice(DeviceItem(scanResult.device, scanResult.isConnectable))
            }else{
                viewModel?.addDevice(DeviceItem(scanResult.device, false))
            }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("BT", "onActivityResult: " + "Request code " + requestCode +  ", Result Code " + resultCode)
        when (requestCode) {
            PermissionsUtils.REQUEST_ENABLE_BT -> if (resultCode == RESULT_OK) {
               Toast.makeText(this, "BLUETOOTH ON BRAVO", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "BLUETOOTH OFF SAD", Toast.LENGTH_SHORT).show()
            }
            PermissionsUtils.REQUEST_ENABLE_LOCATION -> Log.d("BT", "BRRR LOCATIE HAHA :D" )
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }
    @Composable
    fun OneItem(device: DeviceItem){
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Text(text = "${device.device.name} ${device.isConnectable}")
            }else{
                Text(text = "${device.device.name}")
            }
                Button(onClick = {
                btService?.scanForDevices(false)
                if(!btService?.connect(device.device)!!)
                    Toast.makeText(this@HrSettingsActivity, "Couldn't connect. Try disconnecting the wearable from  all other bluetooth devices", Toast.LENGTH_LONG).show()
                else {
                    val intent = Intent(applicationContext, HRMeasurementActivity::class.java)
                    intent.putExtra("device", device.device.address)
                    startActivity(intent)
                }
            }){
                Text(text="Register")
            }
        }
    }
    @Composable
    fun app(){
        val context = LocalContext.current
        MyApplicationTheme {
            Surface(color = MaterialTheme.colors.background) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .fillMaxHeight()
                    .padding(20.dp, 40.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    val locationEnabled = remember { mutableStateOf(false)}
                    val bluetoothEnabled = remember { mutableStateOf(false)}
                    val scanEnabled = remember { mutableStateOf(true)}
                    Button(onClick = {
                        locationEnabled.value = true
                    }, enabled = bluetoothEnabled.value) {
                        Text(text="Enable bluetooth")
                    }
                    Button(onClick = {
                        scanEnabled.value = true
                    }, enabled = locationEnabled.value) {
                        Text(text="Enable location")
                    }

                    Button(onClick = {
                        viewModel?.clearDeviceList()
                        btService?.scanForDevices(false)
                        btService?.scanForDevices(true)
                    }, enabled = scanEnabled.value) {
                        Text(text = "Scan for devices")
                    }
                    val deviceList by viewModel?.scannedDevices!!.observeAsState(mutableListOf())
                    LazyColumn(
                        modifier = Modifier
                            .background(color = Color.Red)
                            .fillMaxSize()
                            .padding(10.dp, 0.dp), verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(deviceList) { item ->
                            OneItem(device = item)
                        }
                    }
                }
            }
        }
    }

}


