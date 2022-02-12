package com.example.myapplication

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.myapplication.service.BluetoothLeService
import kotlin.concurrent.withLock

class BLECommands {
    companion object{
        private val startHeartMeasurementManual =
            byteArrayOf(0x15, 0x02, 1)
        private val stopHeartMeasurementManual =
            byteArrayOf(0x15, 0x02, 0)
        private val startHeartMeasurementContinuous =
            byteArrayOf(0x15, 0x01, 1)
        private val stopHeartMeasurementContinuous =
            byteArrayOf(0x15, 0x01, 0)
        val enableHeartConnection = byteArrayOf(0x06, 0x1f, 0x00, 0x01)

        fun enableHrConnection(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic){
            characteristic.setValue(enableHeartConnection)
            gatt.writeCharacteristic(characteristic)
        }

        fun startHrManual(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic){
            characteristic.setValue(startHeartMeasurementManual)
            gatt.writeCharacteristic(characteristic)
        }
        fun stopHrManual(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic){
            characteristic.setValue(stopHeartMeasurementManual)
            gatt.writeCharacteristic(characteristic)
        }
        fun startHrContinuous(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic){
            characteristic.setValue(startHeartMeasurementContinuous)
            gatt.writeCharacteristic(characteristic)
        }
        fun stopHrContinuous(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic){
            characteristic.setValue(stopHeartMeasurementContinuous)
            gatt.writeCharacteristic(characteristic)
        }

        fun notifyHr(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, enabled: Boolean){
            gatt.setCharacteristicNotification(characteristic, enabled)
        }

        fun setupHrMeasurement(gatt: BluetoothGatt, control: BluetoothGattCharacteristic, measurement: BluetoothGattCharacteristic){
                notifyHr(gatt, measurement, true)
            BluetoothLeService.writeCharacteristicLock.withLock {
                stopHrManual(gatt, control)
                BluetoothLeService.writeCharacteristicCondition.await()
            }
            BluetoothLeService.writeCharacteristicLock.withLock {
                startHrContinuous(gatt, control)
                BluetoothLeService.writeCharacteristicCondition.await()
            }

        }
    }
}