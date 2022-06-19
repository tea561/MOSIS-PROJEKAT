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
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.model.UserViewModel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class BluetoothClientFragment : Fragment() {

    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevices: MutableList<BluetoothDevice>
    private lateinit var listAdapter: ArrayAdapter<BluetoothDevice>
    private val userViewModel: UserViewModel by activityViewModels()

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

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val buttonScan: Button = requireView().findViewById(R.id.buttonScan)
        val pairedDevicesList: ListView = requireView().findViewById(R.id.listViewPairedDevices)
        listAdapter = ArrayAdapter<BluetoothDevice>(view.context, android.R.layout.simple_list_item_1)
        pairedDevicesList.adapter = listAdapter
        pairedDevicesList.onItemClickListener =
            AdapterView.OnItemClickListener { p0, p1, p2, p3 -> TODO("Not yet implemented") }



        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity?.registerReceiver(receiver,filter)

        buttonScan.setOnClickListener {
            if(bluetoothAdapter != null && bluetoothAdapter.isEnabled){
//                val pairedDevices = bluetoothAdapter.bondedDevices
//                pairedDevices?.forEach { device ->
//                    val deviceName = device.name
//                    val deviceHardwareAddress = device.address // MAC address
//                    bluetoothDevices.add(device)
//                }
                bluetoothDevices.clear()
                listAdapter.clear()
                bluetoothAdapter.startDiscovery()

                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                activity?.registerReceiver(receiver,filter)
            }
            else
            {
                Log.e("BluetoothClient", "Bluetooth disabled")
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
                    listAdapter.add(device)
                    listAdapter.notifyDataSetChanged()
                    bluetoothDevices.add(device)
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        activity?.unregisterReceiver(receiver)
        bluetoothAdapter.cancelDiscovery()
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
            val connectionThread: ConnectedThread = ConnectedThread(socket)
            connectionThread.start()
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }

        private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread()
        {
            private val mmInStream: InputStream = mmSocket.inputStream
            private val mmOutStream: OutputStream = mmSocket.outputStream
            private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream


            override fun run() {
                var numBytes: Int // bytes returned from read()
                val uid: String = userViewModel.selectedUser?.uid ?: ""

                //send uid
                try{
                    mmOutStream.write(uid.toByteArray())
                } catch (e: IOException){
                    Log.e(TAG, "Error occurred when sending data", e)
                }

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    // Read from the InputStream.
                    numBytes = try {
                        mmInStream.read(mmBuffer)
                    } catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        break
                    }

                    //TODO: izlaz iz petlje i close connection


                }
            }

            fun cancel() {
                try{
                    mmSocket.close()
                } catch (e: IOException){
                    Log.e(TAG, "Could not close the connect socket", e)
                }
            }

        }
    }



}