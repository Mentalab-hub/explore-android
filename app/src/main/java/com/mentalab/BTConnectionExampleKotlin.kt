package com.mentalab

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

class UUIDObserver(private val deviceName: String, private val tv: TextView) : BroadcastReceiver() {
    override fun onReceive(ctx: Context?, intent: Intent?) {
        when(intent?.action){
            BluetoothDevice.ACTION_FOUND -> {
                var rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE)
                var name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME)
                var dev = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                var cls = intent.getParcelableExtra<BluetoothClass>(BluetoothDevice.EXTRA_CLASS)
                Log.i("UUIDObserver", "Device: "+ dev.toString() + ", Class: "+cls.toString())
                Log.i("UUIDObserver", "RSSI: $rssi, Name: $name")
                if(name==deviceName){
                    val text = "RSSI for $deviceName: $rssi dBm"
                    Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show()
                    tv.text = text
                    tv.invalidate()
                }
            }
        }
    }
}

class BTConnectionExampleKotlin : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var connectButton: Button
    lateinit var lo : UUIDObserver
    private lateinit var socket : BluetoothSocket
    lateinit var btAdapter: BluetoothAdapter
    lateinit var btManager: BluetoothManager
    lateinit var btDevice: BluetoothDevice
    lateinit var isConnectedText: TextView
    var finishedSetup = false

    lateinit var handler: Handler

    fun connectToDevice() {
        if(!this::socket.isInitialized) {
            Log.e("BT", "Socket not initialized, returning...")
            return
        }
        if(!this::btDevice.isInitialized) {
            Log.e("BT", "Bluetooth device not initialized, returning...")
        }
        Log.i("BT", "Trying to connect...")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("BT", "BLUETOOTH_CONNECT Permission not granted, returning...")
            return
        }

        try {
            socket.connect()
            isConnectedText.text = "Connected to ${btDevice.name}"
            isConnectedText.setTextColor(resources.getColor(R.color.green))
            isConnectedText.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("BT", "Could not connect, trying fallback socket...")
            socket = btDevice.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType).invoke(btDevice, 1) as BluetoothSocket
            try {
                socket.connect()
                isConnectedText.text = "Connected to ${btDevice.name}"
                isConnectedText.setTextColor(resources.getColor(R.color.green))
                isConnectedText.invalidate()
            } catch (e: Exception) {
                Log.e("BT", "Could not connect with fallback socket, returning...")
                isConnectedText.text = "Not connected"
                isConnectedText.setTextColor(resources.getColor(R.color.red))
                isConnectedText.invalidate()
                return
            }
        }
    }

    fun connect() {
        if(btAdapter == null){
            Log.i("MainActivity", "Bluetooth Adapter is null.")
            val t: Toast = Toast.makeText(this, "Bluetooth adapter not available.", Toast.LENGTH_SHORT)
            t.show()
            return
        }
        Log.i("MainActivity", "Bluetooth Adapter String rep: $btAdapter")
        val t: Toast = Toast.makeText(this, "Bluetooth adapter available: $btAdapter", Toast.LENGTH_SHORT)
        t.show()

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("BT", "BLUETOOTH_CONNECT Permission not granted, returning...")
            return
        }

        try {
            Log.i("BT", "Creating RFCOMM socket...")
            socket =
                btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"))
        } catch (e: Exception) {
            Log.e("MainActivity", "Could not create rfcomm socket from UUID.")
            return
        }

        btAdapter.cancelDiscovery()

        connectToDevice()
        Log.i("BT", "Successfully connected to: ${btDevice.name}")
        val r: Runnable = object : Runnable {
            var numBytes: Int = 0
            val mmBuffer = ByteArray(1024)
            var inStream = socket!!.inputStream

            override fun run() {
                try {
                    numBytes = inStream.read(mmBuffer)
                    Log.i("INSTREAM", "Received: $numBytes")
                } catch (e: IOException) {
                    Log.d("INSTREAM", "Input stream was disconnected", e)
                    Log.d("INSTREAM", "Attempting to reconnect...")
                    connectToDevice()
                    inStream = socket!!.inputStream
                }
                handler.postDelayed(this, 10) // Read input stream every 10ms
            }
        }

        handler.post(r)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bt_connection_example)

        btManager = this.getSystemService(BluetoothManager::class.java)
        btAdapter = btManager.adapter

        val deviceName = "Explore_CA14"

        connectButton = findViewById<Button>(R.id.connect)
        connectButton.text = "Connect to $deviceName"
        connectButton.setOnClickListener() {
            if(!finishedSetup) {
                Toast.makeText(this, "Not ready to connect yet, try again in a few seconds!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            connect()
            it.isEnabled = false
        }
        textView = findViewById<TextView>(R.id.rssi)
        isConnectedText = findViewById<TextView>(R.id.isConnected)

        handler = Handler(Looper.getMainLooper())

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        lo = UUIDObserver(deviceName, textView)
        registerReceiver(lo, filter)

        val actResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if(activityResult.resultCode == Activity.RESULT_OK){
                Log.i("BT Intent launcher", "Result ok.")
            } else Log.i("BT Intent launcher", "Result not ok: ${activityResult.resultCode}")
        }

        val permissionsToRequest = Array<String>(2){""}
        permissionsToRequest[0] = Manifest.permission.BLUETOOTH_CONNECT
        permissionsToRequest[1] = Manifest.permission.BLUETOOTH_SCAN


        val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionResult ->
            if(permissionResult.containsKey(Manifest.permission.BLUETOOTH_CONNECT) &&
                permissionResult.containsKey(Manifest.permission.BLUETOOTH_SCAN)) {
                if(permissionResult[Manifest.permission.BLUETOOTH_CONNECT]!! &&
                    permissionResult[Manifest.permission.BLUETOOTH_SCAN]!!) {
                    if (btAdapter?.isEnabled == false) {
                        Log.i("BT", "Reached enabled")
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        actResultLauncher.launch(enableBtIntent) // asks to turn on bluetooth
                    }
                }
            }
            for((k, v) in permissionResult) {
                Log.i("Permissions", "Key: $k, Value: $v")
            }
        }

        permissionResultLauncher.launch(permissionsToRequest)

        var btDevices: Set<BluetoothDevice>? = null
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            btDevices = btAdapter?.bondedDevices
        }

        if(btDevices == null) return

        for(e in btDevices) {
            Log.i("Main Activity", "Bluetooth Device: ${e.name}")
        }

        for(e in btDevices) {
            if (deviceName == e.name) {
                btDevice = e
            }
        }

        if(btDevice == null) return

        // Start discovery is necessary to fetch the RSSIs of nearby devices
        btAdapter.startDiscovery()

        val uuids = btDevice.uuids

        for (uuid in uuids) {
            Log.i("UUID", uuid.toString())
        }
        finishedSetup = true
    }
}