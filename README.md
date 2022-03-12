# BLE-Heart-Rate-Reader-
Android app used to read Heart Rate data from Bluetooth Low-Energy devices. Currently very bad and WIP.


To Run:
-1.Make sure Bluetooth is enabled
-2.Make sure Location is enabled
-3.If any permissions are required, please give them
-4.If the app doesn't find Bluetooth devices, try restarting it? 
-5.After registering to a device, after around 1 minute some numbers should appear on the screen

It sends the collected data through a REST API to a web server, which saves it to a local mysql database. The details for this server are found in /resources/config.properties. The entity contains the heart rate value, the MAC of the wearable device, the timestamp of the measurement, and a "login token", which is an attempt at basic security measures (you'd need to know a certain username and password of the databse).
