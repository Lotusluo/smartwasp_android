package com.smartwasp.assistant.blesdk

import android.app.Application

// Created by luotao on 2021/3/29
interface IBluetoothClient {
    companion object{
        //默认的蓝牙设备名称
        const val PREFIX_DEVICE_NAME = "rockchip"
        //接口单例
        private var instance:IBluetoothClient? = null

        internal lateinit var appEnt:Application
        /**
         * 初始化蓝牙SDK
         * @param app 为application的上下文
         */
        fun init(app:Application){
            appEnt = app
            if(null == instance){
                synchronized(IBluetoothClient::class){
                    instance = BluetoothClient()
                }
            }
        }

        //获取单例
        fun getInstance():IBluetoothClient{
            return instance!!
        }
    }

    /**
     * 开始扫描
     * @param leScanCallback 搜索监听器
     * @return 是否能够扫描
     */
    fun startScan(leScanCallback:ILeScanCallback):Boolean

    /**
     * 停止扫描
     *
     */
    fun stopScan()

    /**
     * 开始与蓝牙设备建立连接
     * @param device 需要连接的设备
     * @param connectCallback
     */
    fun connect(device: LeDevice,connectCallback: ILeConnectCallback)

    /**
     * 搜索可用的wifi列表
     * @param wifiScanCallback wifi回调器
     */
    fun startWifiScan(wifiScanCallback:IWifiScanCallback)

    /**
     * 蓝牙配网
     * @param ssid 网络名
     * @param pwd 网络密码
     * @param wifiSetupCallback
     */
    fun startWifiSetup(ssid:String,pwd:String,
                       wifiSetupCallback: IWifiSetupCallback)

    /**
     * 断开连接
     */
    fun disconnect()

}
