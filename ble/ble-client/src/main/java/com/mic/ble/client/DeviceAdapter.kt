package com.mic.ble.client

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mic.ble.client.databinding.ItemDeviceBinding

/**
 * BLE 设备列表适配器
 *
 * 将扫描到的蓝牙设备显示在 RecyclerView 中
 * 每个列表项显示设备名称和 MAC 地址
 * 用户点击设备时，触发选中回调
 *
 * @param items 要显示的设备列表
 */
class DeviceAdapter(private val items: List<DeviceItem>) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    /**
     * 设备列表项数据模型
     *
     * @property device 蓝牙设备对象
     * @property onSelected 用户选中此设备时的回调
     */
    data class DeviceItem(
        val device: BluetoothDevice,
        val onSelected: (DeviceItem) -> Unit
    )

    /**
     * 设备列表项的 ViewHolder
     *
     * 使用 ViewBinding 进行视图绑定，避免 findViewById 的繁琐和类型不安全
     *
     * @property binding 设备列表项的视图绑定对象
     */
    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * 将数据绑定到视图
         *
         * 设置设备名称和 MAC 地址，配置点击监听器
         *
         * @param item 要显示的设备项
         */
        fun bind(item: DeviceItem) {
            binding.apply {
                // 设置设备名称（如果没有名称则显示 "Unknown"）
                textDeviceName.text = item.device.name ?: "未知"

                // 设置设备 MAC 地址
                textDeviceAddress.text = item.device.address

                // 设置点击监听器，用户点击时触发回调
                root.setOnClickListener {
                    item.onSelected(item)
                }
            }
        }
    }

    /**
     * 创建 ViewHolder
     *
     * 当 RecyclerView 需要新的视图持有者时调用
     * 通过 ViewBinding 创建视图
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    /**
     * 绑定数据到 ViewHolder
     *
     * 当 RecyclerView 需要显示或更新列表项时调用
     */
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /**
     * 获取列表项数量
     */
    override fun getItemCount(): Int = items.size
}
