package com.smartwasp.assistant.blesdk

// Created by luotao on 2021/3/29
interface ILeScanCallback {
    /**
     * 搜索到的蓝牙回调
     * @param device
     */
    fun onLeScan(device: LeDevice)

    /**
     * 停止搜索
     */
    fun onLeScanStop()
}

interface ILeConnectCallback{
    /**
     * 建立成功回调
     */
    fun onConnectSuccess()
    /**
     * 建立失败回调
     */
    fun onConnectClose()
}

interface IWifiScanCallback {
    /**
     * 搜索到的蓝牙回调
     * @param device
     * @param wifiList
     */
    fun onWifiScan(wifiList: List<WifiInfo>)
}

interface IWifiSetupCallback {
    /**
     * 蓝牙配网成功
     */
    fun onWifiSetupSuccess()
    /**
     * 蓝牙配网失败
     */
    fun onWifiSetupFail()
}
