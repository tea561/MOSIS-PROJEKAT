package elfak.mosis.capturetheflag.friends

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentBTBinding
import elfak.mosis.capturetheflag.databinding.FragmentGameOverBinding


class BTFragment : Fragment() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var _binding: FragmentBTBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_ENABLE_BT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBTBinding.inflate(inflater, container, false)

        val bluetoothManager: BluetoothManager? = ContextCompat.getSystemService(requireContext(), BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager!!.adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkBTState()

        binding.buttonFind.setOnClickListener {
            if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled)
            {
                checkBTState()
            }
            else {
                findNavController().navigate(R.id.action_BTFragment_to_BluetoothClientFragment)
            }
        }

        binding.buttonLet.setOnClickListener {
            if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled)
            {
                checkBTState()
            }
            else {
                findNavController().navigate(R.id.action_BTFragment_to_BluetoothServerFragment)
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


    private fun checkBTState(){
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(requireContext(), "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
        }
        else {
            if (!bluetoothAdapter.isEnabled) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Bluetooth enabled.", Toast.LENGTH_SHORT).show()
        }
    }

}