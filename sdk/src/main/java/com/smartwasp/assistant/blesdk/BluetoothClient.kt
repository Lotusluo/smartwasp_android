package com.smartwasp.assistant.blesdk

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import java.lang.ref.WeakReference
import java.util.*

// Created by luotao on 2021/3/29
internal class BluetoothClient:IBluetoothClient {
    companion object{
        const val TAG = "BluetoothClient"
        /**
         * UUID of Wifi Service
         */
        val WIFI_SERVICE_UUID = UUID
                .fromString("0000180A-0000-1000-8000-00805F9B34FB")

        /**
         * UUID of Wifi Characteristic
         */
        val WIFI_CHARACTERISTIC_UUID = UUID
                .fromString("00009999-0000-1000-8000-00805F9B34FB")

        /**
         * UUID of the client configuration descriptor
         */
        val CLIENT_CONFIG_DESCRIPTOR_UUID = UUID
                .fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
    private var mBluetoothAdapter: BluetoothAdapter? = null


    //是否在扫描中
    private var mIsLeScanning = false

    //扫描监听器
    private var mLeScanCallback: LeScanCallback? = null

    //初始化sdk客户端
    init {
        try {
            val bluetoothManager = IBluetoothClient.appEnt.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mBluetoothAdapter = bluetoothManager.adapter
            Log.d(TAG,"Init ble sdk ok")
        }catch (e:Throwable){
            Log.e(TAG,"Init ble sdk err:${e.message}")
        }
    }

    /**
     * 开始扫描
     */
    override fun startScan(leScanCallback:ILeScanCallback): Boolean {
        stopScan()
        mBluetoothAdapter ?: return false
        mOutsideLeScanCallback = WeakReference(leScanCallback)
        if(!mBluetoothAdapter!!.isEnabled){
            mBluetoothAdapter!!.enable()
            return false
        }
        if(null == mLeScanCallback){
            mLeScanCallback = LeScanCallback{ bluetoothDevice: BluetoothDevice,
                                              rssi: Int,
                                              scanRecord: ByteArray ->
                if(bluetoothDevice.name.isNullOrEmpty())
                    return@LeScanCallback
                mOutsideLeScanCallback?.get()?.onLeScan(LeDevice(bluetoothDevice,rssi,scanRecord))
            }
        }
        //旧接口相对于新接口，扫描速度更快
        mIsLeScanning = mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        AppExecutors.get().mainThread().executeDelay(delayStopScan,10 * 1000)
        return mIsLeScanning
    }private var mOutsideLeScanCallback:WeakReference<ILeScanCallback>? = null

    /**
     * 停止扫描
     */
    override fun stopScan() {
        AppExecutors.get().mainThread().removeCallbacks(delayStopScan)
        mBluetoothAdapter ?: return
        mLeScanCallback ?: return
        mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
        mOutsideLeScanCallback?.get()?.onLeScanStop()
        mOutsideLeScanCallback = null
        mIsLeScanning = false
    }
    //延迟关闭蓝牙搜索
    private var delayStopScan = Runnable {
        stopScan()
    }

    //暂时只留取一个会话
    private var mSession:Session? = null
    private var isConnecting:Boolean = false
    /**
     * 开始建立连接
     */
    override fun connect(device: LeDevice,connectCallback: ILeConnectCallback) {
        stopScan()
        mBluetoothAdapter ?: return
        if(isConnecting){
            Log.d(TAG,"isConnecting!")
            return
        }
        mSession?.let {
            Log.d(TAG,"has session!")
        }?: kotlin.run {
            isConnecting = true
            Session.acceptGatt(mBluetoothAdapter!!,device,object :Session.ILeSessionCallback{
                override fun onSessionOpen(session: Session) {
                    isConnecting = false
                    Log.d(TAG,"onSessionSuccess:$session")
                    mSession = session
                    AppExecutors.get().mainThread().execute {
                        connectCallback.onConnectSuccess()
                    }
                }
                override fun onSessionClose(session: Session) {
                    isConnecting = false
                    Log.d(TAG,"onSessionErrorOrClose:$session")
                    if(mSession == session){
                        mSession = null
                    }
                    AppExecutors.get().mainThread().execute {
                        connectCallback.onConnectClose()
                    }
                }
            })
        }
    }

    /**
     * 开启获取wifi列表
     * @param wifiScanCallback 回调器
     */
    override fun startWifiScan(wifiScanCallback: IWifiScanCallback) {
        if(null == mSession){
            Log.d(TAG,"mSession is null!")
            return
        }
        mSession!!.notifyGatt()
        mSession!!.startWifiScan(wifiScanCallback)
    }


    /**
     * 蓝牙配网
     */
    override fun startWifiSetup(ssid: String, pwd: String,
                                wifiSetupCallback: IWifiSetupCallback) {
        if(null == mSession){
            Log.d(TAG,"mSession is null!")
            return
        }
        mSession!!.notifyGatt()
        mSession!!.startWifiSetup(ssid,pwd,wifiSetupCallback)
    }

    /**
     * 断开现有连接
     */
    override fun disconnect() {
        if(null == mSession){
            Log.d(TAG,"mSession is null!")
            return
        }
        mSession!!.disconnect()
    }
}