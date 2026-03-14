package com.mic.ble.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mic.ble.client.databinding.ItemDeviceBinding

/**
 * BLE 设备列表适配器
 */
class DeviceAdapter(private val items: List<DeviceItem>) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    data class DeviceItem(
        val device: BluetoothDevice,
        val displayName: String,
        val rssi: Int = 0,
        val onSelected: (DeviceItem) -> Unit
    )

    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun bind(item: DeviceItem) {
            binding.apply {
                textDeviceName.text = item.displayName
                textDeviceAddress.text = "${item.device.address}  RSSI: ${item.rssi}"
                root.setOnClickListener {
                    item.onSelected(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
