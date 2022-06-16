package elfak.mosis.capturetheflag.friends

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import elfak.mosis.capturetheflag.R
import java.io.IOException
import java.util.*


class BluetoothClientFragment : Fragment() {

    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevices: MutableList<BluetoothDevice>
    private lateinit var listAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bluetooth, container, false)

        val bluetoothManager: BluetoothManager? = ContextCompat.getSystemService(requireContext(), BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager!!.adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonScan: Button = requireView().findViewById(R.id.buttonScan)
        val pairedDevicesList: ListView = requireView().findViewById(R.id.listViewPairedDevices)
        listAdapter = ArrayAdapter<String>(view.context, android.R.layout.simple_list_item_1)
        pairedDevicesList.adapter = listAdapter

        checkBTState()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity?.registerReceiver(receiver,filter)

        buttonScan.setOnClickListener {
            if(bluetoothAdapter != null && bluetoothAdapter.isEnabled){
                val permission: String = requiredPermissions()[0]
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(requireActivity(), requiredPermissions(), 1)
                    return@setOnClickListener
                }
                listAdapter.clear()
                bluetoothAdapter.startDiscovery()
            }
            else
            {
                checkBTState()
            }
        }
    }

    private fun checkBTState(){
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(requireContext(), "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
        }
        else {
            if (!bluetoothAdapter?.isEnabled) {
                Toast.makeText(requireContext(), "You need to enable Bluetooth.", Toast.LENGTH_SHORT)
                    .show()
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                try {
                    activity?.startActivityFromFragment(this, enableBtIntent, REQUEST_ENABLE_BT)
                } catch (e: ActivityNotFoundException) {
                    // display error state to the user
                }
            } else {
                val permission: String = requiredPermissions()[0]
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(requireActivity(), requiredPermissions(), 1)
                    return
                }
                if (bluetoothAdapter.isDiscovering) {
                    Toast.makeText(
                        requireContext(),
                        "Device discovering process...",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "Bluetooth enabled.", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    private fun pairedDeviceList()
    {
        val pairedDevices = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            bluetoothDevices.add(device)
        }

        bluetoothAdapter!!.startDiscovery()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity?.registerReceiver(receiver,filter)

    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action!!) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    listAdapter.add("$deviceName $deviceHardwareAddress")
                    listAdapter.notifyDataSetChanged()
                    bluetoothDevices.add(device)
                }
            }
        }
    }

    private fun requiredPermissions(): Array<String>{
        val sdkVersion: Int = requireActivity().applicationInfo.targetSdkVersion
        //android 12
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && sdkVersion >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        //android 9 and lower
        else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && sdkVersion <= Build.VERSION_CODES.P){
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Bluetooth enabled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(receiver)
        super.onDestroy()

    }


    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString("505961ad-0e61-4de4-a0d9-313c06572d09"))
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket)
            }
        }

        private fun manageMyConnectedSocket(socket: BluetoothSocket) {
            TODO("Not yet implemented")
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }



}