package com.smartwasp.assistant.blesdk

import android.bluetooth.BluetoothDevice

// Created by luotao on 2021/3/29
data class LeDevice(private var bluetoothDevice: BluetoothDevice,
                    private var rssi: Int,
                    private var scanRecord: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LeDevice
        if (bluetoothDevice.address != other.bluetoothDevice.address) return false
        return true
    }
    override fun hashCode(): Int {
        return bluetoothDevice.hashCode()
    }

    /**
     * 获取蓝牙物理地址
     * @return 地址
     */
    fun getAddress():String{
        return bluetoothDevice.address
    }

    /**
     * 获取蓝牙设备名称
     * @return 名称
     */
    fun getName():String{
        return if(bluetoothDevice.name.isNullOrEmpty()) "Unknow" else bluetoothDevice.name
    }
}