package com.example.myapplication.utils

import android.Manifest
import android.app.Activity
import android.util.Log
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class PermissionsUtils {
    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_ENABLE_LOCATION  = 2
        const val REQUEST_READ_STORAGE = 3
        const val REQUEST_LOCATION = 4

        val BLUETOOTH_PERMISSIONS_S =arrayOf(Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT)

        @AfterPermissionGranted(REQUEST_ENABLE_BT)
        fun requestBTPermission(activity: Activity?) {
            val perms = arrayOf(Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT)
            if (!EasyPermissions.hasPermissions(activity!!, *perms)) {
                Log.d(
                    "BT",
                    "requestBTPermission: Application is requesting bluetooth permissions"
                )
                EasyPermissions.requestPermissions(
                    activity,
                    "Please grant the bluetooth permission",
                    REQUEST_ENABLE_BT,
                    *perms
                )
            }
        }

        @AfterPermissionGranted(REQUEST_LOCATION)
        fun requestLocationPermission(activity: Activity?) {
            val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (!EasyPermissions.hasPermissions(activity!!, *perms)) {
                Log.d(
                    "BT",
                    "requestLocationPermission: Application is requesting location permissions"
                )
                EasyPermissions.requestPermissions(
                    activity,
                    "Please grant the location permission",
                    REQUEST_LOCATION,
                    *perms
                )
            }
        }

        @AfterPermissionGranted(REQUEST_READ_STORAGE)
        fun requestReadStoragePermission(activity: Activity?) {
            val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (!EasyPermissions.hasPermissions(activity!!, *perms)) {
                Log.d(
                    "BT",
                    "requestReadStoragePermission: Application is requestig external storage permissions"
                )
                EasyPermissions.requestPermissions(
                    activity,
                    "Please grant the read external storage permission",
                    REQUEST_READ_STORAGE,
                    *perms
                )
            }
        }
    }
}