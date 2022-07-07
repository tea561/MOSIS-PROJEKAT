package elfak.mosis.capturetheflag.friends

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import elfak.mosis.capturetheflag.R

class BTDeviceAdapter(private val context: Context, private val bluetoothDevices: MutableList<BluetoothDevice>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return bluetoothDevices.size
    }

    override fun getItem(p0: Int): Any {
        return bluetoothDevices[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val itemView = inflater.inflate(R.layout.bluetooth_device, p2, false)
        val deviceName: TextView = itemView.findViewById(R.id.textViewBTName)
        val deviceAddress: TextView = itemView.findViewById(R.id.textViewBTAddress)

        deviceName.text = bluetoothDevices[p0].name
        deviceAddress.text = bluetoothDevices[p0].address

        return itemView
    }

    fun add(device: BluetoothDevice): Unit{
        val temp = bluetoothDevices.find { btd -> btd.name == device.name && btd.address == device.address }
        if(temp == null)
            bluetoothDevices.add(device)
    }

    fun clear(): Unit{
        bluetoothDevices.clear()
    }


}